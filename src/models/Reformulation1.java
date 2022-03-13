package models;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import config.GSet;
import config.Model;

import static config.Model.*;
import java.io.*;
import static config.Const.*;


public class Reformulation1 {

    public void start(Model model, GSet G, double MIP) throws IloException {
        // define new model
        IloCplex cplex = new IloCplex();
        if (MIP!=0)
            cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, MIP);
        cplex.setParam(IloCplex.Param.MIP.Strategy.Search,
                IloCplex.MIPSearch.Traditional);

        // variables
        IloIntVar[][][] z = new IloIntVar[R][N][N];
        IloIntVar[][][] y = new IloIntVar[N][N][N];
        for (int k = 0; k < R; k++) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    z[k][i][j] = cplex.intVar(0, 1);
                }
            }
        }
        for (int t = 0; t < N; t++) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    y[t][i][j] = cplex.intVar(0, 1);
                }
            }
        }

        // define objective
        //(1)
        IloLinearNumExpr objective = cplex.linearNumExpr();
        for (int t = 0; t < N; t++) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    objective.addTerm(model.distanceMatrix[i][j], y[t][i][j]);
                }
            }
        }
        cplex.addMinimize(objective);

        //(2)(3)
        IloLinearIntExpr[] second= new IloLinearIntExpr[G.length()];
        IloLinearIntExpr[] third= new IloLinearIntExpr[G.length()];
        for (int g = 0; g < G.length(); g++) {
            second[g] = cplex.linearIntExpr();
            third[g] = cplex.linearIntExpr();
            for (int k = 0; k < R; k++) {
                second[g].addTerm(1, z[k][G.pairs[g].i][G.pairs[g].j]);
                third[g].addTerm(1, z[k][G.pairs[g].j][G.pairs[g].i]);
            }
            cplex.addEq(second[g], 1);
            cplex.addEq(third[g], 0);
        }
        //(4)
        IloLinearIntExpr[][] fourth = new IloLinearIntExpr[R][N];
        for (int k = 0; k < R; k++) {
            for (int t = 0; t < N; t++) {
                fourth[k][t] = cplex.linearIntExpr();
                for (int j = 0; j < N; j++) {
                    if (t != j) {
                        fourth[k][t].addTerm(1, z[k][t][j]);
                        fourth[k][t].addTerm(1, z[k][j][t]);
                    }
                }
                cplex.addEq(fourth[k][t], 1);
            }
        }
        //(5)
        IloLinearIntExpr[] fifth = new IloLinearIntExpr[(R - 1) * N * (N - 1) * (N - 2)];
        int count = 0;
        IloIntVar one = cplex.intVar(1, 1);
        for (int k = 1; k < R; k++) {
            for (int t = 0; t < N; t++) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        if ((i != j) && (j != t) && (t != i)) {
                            fifth[count] = cplex.linearIntExpr();
                            fifth[count].addTerm(1, z[k - 1][i][t]);
                            fifth[count].addTerm(1, z[k][j][t]);
                            fifth[count].addTerm(-1, one);
                            cplex.addGe(y[t][i][j], fifth[count]);
                            count++;
                        }
                    }
                }
            }
        }
        //(6)
        IloLinearIntExpr[] sixth = new IloLinearIntExpr[(R - 1) * N * (N - 1)];
        count = 0;
        for (int k = 1; k < R; k++) {
            for (int t = 0; t < N; t++) {
                for (int i = 0; i < N; i++) {
                    if (t != i) {
                        sixth[count] = cplex.linearIntExpr();
                        sixth[count].addTerm(1, z[k - 1][i][t]);
                        sixth[count].addTerm(-1, one);
                        for (int j = 0; j < N; j++) {
                            if (t != j) {
                                sixth[count].addTerm(1, z[k][t][j]);
                            }
                        }
                        cplex.addGe(y[t][i][t], sixth[count]);
                        count++;
                    }
                }
            }
        }
        //(7)
        IloLinearIntExpr[] seventh = new IloLinearIntExpr[(R - 1) * N * (N - 1)];
        count = 0;
        for (int k = 1; k < R; k++) {
            for (int t = 0; t < N; t++) {
                for (int i = 0; i < N; i++) {
                    if (t != i) {
                        seventh[count] = cplex.linearIntExpr();
                        seventh[count].addTerm(1, z[k][i][t]);
                        seventh[count].addTerm(-1,one);
                        for (int j = 0; j < N; j++) {
                            if (t != j) {
                                seventh[count].addTerm(1, z[k - 1][t][j]);
                            }
                        }
                        cplex.addGe(y[t][t][i], seventh[count]);
                        count++;
                    }
                }
            }
        }
        //(8)
        for (int t = 0; t < N; t++) {
            for (int i = 0; i < N; i++) {
                if (t != i) {
                    cplex.addGe(y[t][t][i], z[0][i][t]);
                }
            }
        }
        //(9)
        for (int t = 0; t < N; t++) {
            for (int i = 0; i < N; i++) {
                if (t != i) {
                    cplex.addGe(y[t][i][t], z[R - 1][i][t]);
                }
            }
        }
        //(10)
        IloLinearIntExpr[] tenth = new IloLinearIntExpr[(R - U) * N];
        count = 0;
        for (int k = 0; k < R - U; k++) {
            for (int t = 0; t < N; t++) {
                tenth[count] = cplex.linearIntExpr();
                for (int q = k; q < k + 4; q++) {
                    for (int j = 0; j < N; j++) {
                        if (t != j) {
                            tenth[count].addTerm(1, z[q][j][t]);
                        }
                    }
                }
                cplex.addLe(tenth[count], U);
                cplex.addGe(tenth[count], L);
                count++;
            }
        }


        double time=cplex.getCplexTime();
        if (cplex.solve()) {
            double leadTime=cplex.getCplexTime()-time;
            double obj = cplex.getObjValue();

            int[][] schedule = new int[N][R];
            char[][] HAP = new char[N][R];
            for (int k = 0; k < R; k++) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        if(i!=j) {
                            if (cplex.getValue(z[k][i][j]) >= 0.5) {
                                schedule[i][k] = j;
                                schedule[j][k] = i;
                                HAP[i][k] = 'H';
                                HAP[j][k] = 'A';
                            }
                        }
                    }
                }
                System.out.println();
            }
            System.out.println();
            System.out.println("Objective = "+cplex.getObjValue());
            System.out.print("Distance calculated by the schedule: ");
            checkDistance(schedule,HAP,model);
            System.out.println();
            System.out.println("Is valid schedule: "+ isValidSchedule(schedule));
            System.out.println("Is valid HAP: "+ isValidHAP(HAP));
            System.out.println();
            printOpponents(schedule);
            System.out.println();
            printHAP(HAP);
            System.out.println();
            writeResult(saveResultRef1,schedule,HAP,obj,leadTime,model);
        } else System.out.println("problem not solved");
        cplex.end();

    }
}