package models;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import config.Model;

import java.io.*;

import static config.Model.*;
import static config.Const.*;

public class Basic {

    public void start(Model model, double MIP) throws IOException, IloException {
        // define new model
        IloCplex cplex = new IloCplex();
        if (MIP!=0)
            cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, MIP);
        cplex.setParam(IloCplex.Param.MIP.Strategy.Search,
                IloCplex.MIPSearch.Traditional);
        // variables
        IloIntVar[][][] x = new IloIntVar[R][N][N];
        IloIntVar[][][] z = new IloIntVar[R][N][N];
        IloIntVar[][][][] y = new IloIntVar[R][N][N][N];
        for (int k = 0; k < R; k++) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    x[k][i][j] = cplex.intVar(0, 1);
                    z[k][i][j] = cplex.intVar(0, 1);
                }
            }
        }
        for (int k = 0; k < R; k++) {
            for (int t = 0; t < N; t++) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        y[k][t][i][j] = cplex.intVar(0, 1);
                    }
                }
            }
        }
        // define objective
        //(1)
        IloLinearNumExpr objective = cplex.linearNumExpr();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                objective.addTerm(model.distanceMatrix[i][j], x[0][i][j]);
            }
        }
        for (int k = 0; k < R - 1; k++) {
            for (int t = 0; t < N; t++) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        objective.addTerm(model.distanceMatrix[i][j], y[k][t][i][j]);
                    }
                }
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                objective.addTerm(model.distanceMatrix[j][i], x[R - 1][i][j]);
            }
        }
        cplex.addMinimize(objective);

        // constraints
        //(2)
        for (int k = 0; k < R; k++) {
            for (int i = 0; i < N; i++) {
                cplex.addEq(x[k][i][i], 0);
            }
        }
        //(3)
        IloLinearIntExpr[] third = new IloLinearIntExpr[N * R];
        int count = 0;
        for (int k = 0; k < R; k++) {
            for (int i = 0; i < N; i++) {
                third[count] = cplex.linearIntExpr();
                for (int j = 0; j < N; j++) {
                    third[count].addTerm(1, x[k][i][j]);
                    third[count].addTerm(1, x[k][j][i]);
                }
                cplex.addEq(third[count], 1);
                count++;
            }
        }
        //(4)
        IloLinearIntExpr[] fourth = new IloLinearIntExpr[N * (N - 1)];
        count = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i != j) {
                    fourth[count] = cplex.linearIntExpr();
                    for (int k = 0; k < R; k++) {
                        fourth[count].addTerm(1, x[k][i][j]);
                        fourth[count].addTerm(1, x[k][j][i]);
                    }
                    cplex.addEq(fourth[count], 1);
                    count++;
                }
            }
        }
        //(5)
        IloLinearIntExpr[] fifth = new IloLinearIntExpr[N * (R - U)];
        count = 0;
        for (int k = 0; k < R - U; k++) {
            for (int i = 0; i < N; i++) {
                fifth[count] = cplex.linearIntExpr();
                for (int l = 0; l < U+1; l++) {
                    for (int j = 0; j < N; j++) {
                        fifth[count].addTerm(1, x[k + l][i][j]);
                    }
                }
                cplex.addLe(fifth[count], U);
                cplex.addGe(fifth[count], L);
                count++;
            }
        }
        //(6)
        IloLinearIntExpr[] sixth = new IloLinearIntExpr[N * (R - 1) * N];
        count = 0;
        for (int k = 0; k < R - 1; k++) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    sixth[count] = cplex.linearIntExpr();
                    sixth[count].addTerm(1, x[k][i][j]);
                    sixth[count].addTerm(1, x[k][j][i]);
                    sixth[count].addTerm(1, x[k + 1][i][j]);
                    sixth[count].addTerm(1, x[k + 1][j][i]);
                    cplex.addLe(sixth[count], 1);
                    count++;
                }
            }
        }
        //(7)
        IloLinearIntExpr[] seventh = new IloLinearIntExpr[N * R];
        count = 0;
        for (int k = 0; k < R; k++) {
            for (int i = 0; i < N; i++) {
                seventh[count] = cplex.linearIntExpr();
                for (int j = 0; j < N; j++) {
                    seventh[count].addTerm(1, x[k][j][i]);
                }
                cplex.addEq(seventh[count], z[k][i][i]);
                count++;
            }
        }
        //(8)
        for (int k = 0; k < R; k++) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (i != j)
                        cplex.addEq(z[k][i][j], x[k][i][j]);
                }
            }
        }
        //(9)
        IloLinearIntExpr[] ninth = new IloLinearIntExpr[N * N * N * (R - 1)];
        count = 0;
        for (int k = 0; k < R - 1; k++) {
            for (int t = 0; t < N; t++) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        ninth[count] = cplex.linearIntExpr();
                        ninth[count].addTerm(-1, z[k][t][i]);
                        ninth[count].addTerm(-1, z[k + 1][t][j]);
                        ninth[count].addTerm(1,y[k][t][i][j]);
                        cplex.addGe(ninth[count],-1);
                        count++;
                    }
                }
            }
        }


        double time=cplex.getCplexTime();
        if (cplex.solve()) {
            double leadTime=cplex.getCplexTime()-time;
            double obj = cplex.getObjValue();
            int[][] schedule = new int[N][R];
            char[][] HAP = new char[N][R];
            FileWriter fw = new FileWriter(gPath);
            for (int k = 0; k < R; k++) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        if(cplex.getValue(x[k][i][j])>=0.5){
                            schedule[i][k]=j;
                            schedule[j][k]=i;
                            HAP[i][k]='A';
                            HAP[j][k]='H';
                            fw.write(j  + " " + i);
                            fw.append("\n");
                        }
                    }
                }
            }
            fw.flush();
            System.out.println();
            System.out.println("Objective = "+cplex.getObjValue());
            System.out.print("Distance calculated by the schedule: ");
            checkDistance(schedule,HAP,model);
            System.out.println();
            System.out.println("Is valid schedule: "+isValidSchedule(schedule));
            System.out.println("Is valid HAP: "+isValidHAP(HAP));
            System.out.println();
            printOpponents(schedule);
            System.out.println();
            printHAP(HAP);
            System.out.println();
            writeResult(saveResultBasic,schedule,HAP,obj,leadTime,model);
        }
        else System.out.println("problem not solved");
        cplex.end();
    }

}