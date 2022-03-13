package models;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import config.GSet;
import config.Model;

import static config.Const.*;
import static config.Model.*;
import java.io.*;

public class Reformulation2 {
    private Model model;
    private int c(int i, int j) {
        return model.distanceMatrix[i][j] + model.distanceMatrix[j][i];
    }
    private int c(int i, int j, int m) {
        return model.distanceMatrix[i][j] + model.distanceMatrix[j][m] + model.distanceMatrix[m][i];
    }
    private int c(int i, int j, int m, int l) {
        return model.distanceMatrix[i][j] + model.distanceMatrix[j][m] + model.distanceMatrix[m][l] + model.distanceMatrix[l][i];
    }
    public double start(Model model, GSet G, double MIP)   {
        this.model=model;
        // define new model
        IloCplex cplex = null;

        try {
            cplex = new IloCplex();
            cplex.setOut(null);
        if (MIP!=0)
            cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, MIP);
        cplex.setParam(IloCplex.Param.MIP.Strategy.Search,
                IloCplex.MIPSearch.Traditional);

        // variables
        IloIntVar[][][] w1 = new IloIntVar[R + 2][N][N];
        IloIntVar[][][][] w2 = new IloIntVar[R + 2][N][N][N];
        IloIntVar[][][][][] w3 = new IloIntVar[R + 2][N][N][N][N];
        for (int k = 0; k < R+2; k++) {
            for (int t = 0; t < N; t++) {
                for (int i = 0; i < N; i++) {
                    if(t!=i) {
                        w1[k][t][i] = cplex.intVar(0, 1);
                        for (int j = 0; j < N; j++) {
                            if(t!=j && i!=j) {
                                w2[k][t][i][j] = cplex.intVar(0, 1);
                                for (int l = 0; l < N; l++) {
                                    if(l!=j && l!=i && l!=t)
                                        w3[k][t][i][j][l] = cplex.intVar(0, 1);
                                    else w3[k][t][i][j][l] = cplex.intVar(0, 0);
                                }
                            }else{
                                w2[k][t][i][j] = cplex.intVar(0, 0);
                                for (int l = 0; l < N; l++) {
                                    w3[k][t][i][j][l] = cplex.intVar(0, 0);
                                }
                            }
                        }
                    }else{
                        w1[k][t][i] = cplex.intVar(0, 0);
                        for (int j = 0; j < N; j++) {
                            w2[k][t][i][j] = cplex.intVar(0, 0);
                            for (int l = 0; l < N; l++) {
                                w3[k][t][i][j][l] = cplex.intVar(0, 0);
                            }
                        }
                    }
                }
            }
        }
        // define objective
        //(1)
        IloLinearNumExpr objective = cplex.linearNumExpr();
        for (int k = 0; k < R; k++) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (i != j) {
                        if (G.belongsG(j, i)) {
                            objective.addTerm(c(i, j), w1[k][i][j]);
                            for (int m = 0; m < N; m++) {
                                if (m != j) {
                                    if (G.belongsG(m, i)) {
                                        objective.addTerm(c(i, j, m), w2[k][i][j][m]);
                                        for (int l = 0; l < N; l++) {
                                            if (m != j && m != l && l != j) {
                                                if (G.belongsG(l, i)) {
                                                    objective.addTerm(c(i, j, m, l), w3[k][i][j][m][l]);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        cplex.addMinimize(objective);

        //(2)
        IloLinearIntExpr second = cplex.linearIntExpr();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (G.belongsG(j, i)) {
                    for (int q = R; q < R+2; q++) {
                        second.addTerm(1, w1[q][i][j]);
                    }
                    for (int m = 0; m < N; m++) {
                        if (m != j) {
                            if (G.belongsG(m, i)) {
                                for (int w = R - 1; w < R + 2; w++) {
                                    second.addTerm(1, w2[w][i][j][m]);
                                }
                                for (int l = 0; l < N; l++) {
                                    if (m != j && m != l && l != j) {
                                        if (G.belongsG(l, i)) {
                                            for (int e = R - 2; e < R + 2; e++) {
                                                second.addTerm(1, w3[e][i][j][m][l]);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        cplex.addEq(second, 0);

        //(3)
        IloLinearIntExpr[] third = new IloLinearIntExpr[G.length()];
        for (int g = 0; g < G.length(); g++) {
            third[g] = cplex.linearIntExpr();
            for (int k = 0; k < R; k++) {
                third[g].addTerm(1, w1[k][G.pairs[g].j][G.pairs[g].i]);
                for (int m = 0; m < N; m++) {
                    if (m != G.pairs[g].i) {
                        if (G.belongsG(m, G.pairs[g].j)) {
                            third[g].addTerm(1, w2[k][G.pairs[g].j][G.pairs[g].i][m]);
                            third[g].addTerm(1, w2[k][G.pairs[g].j][m][G.pairs[g].i]);
                            for (int l = 0; l < N; l++) {
                                if (m != G.pairs[g].i && m != l && l != G.pairs[g].i) {
                                    if (G.belongsG(l, G.pairs[g].j)) {
                                        third[g].addTerm(1, w3[k][G.pairs[g].j][G.pairs[g].i][m][l]);
                                        third[g].addTerm(1, w3[k][G.pairs[g].j][m][G.pairs[g].i][l]);
                                        third[g].addTerm(1, w3[k][G.pairs[g].j][m][l][G.pairs[g].i]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            cplex.addEq(third[g], 1);
        }

        //(4)
        IloLinearIntExpr[][] fourth = new IloLinearIntExpr[R][N];
        for (int k = 0; k < R; k++) {
            for (int i = 0; i < N; i++) {
                fourth[k][i]=cplex.linearIntExpr();
                for (int j = 0; j < N; j++) {
                    if (G.belongsG(j, i)) {
                        fourth[k][i].addTerm(1, w1[k][i][j]);
                        for (int m = 0; m < N; m++) {
                            if (m != j) {
                                if (G.belongsG(m, i)) {
                                    if (k >= 1) {
                                        fourth[k][i].addTerm(1, w2[k][i][j][m]);
                                        fourth[k][i].addTerm(1, w2[k - 1][i][j][m]);
                                    } else {
                                        fourth[k][i].addTerm(1, w2[0][i][j][m]);
                                        fourth[k][i].addTerm(1, w2[R + 1][i][j][m]);
                                    }
                                    for (int l = 0; l < N; l++) {
                                        if (m != j && m != l && l != j) {
                                            if (G.belongsG(l, i)) {
                                                if (k >= 2) {
                                                    fourth[k][i].addTerm(1, w3[k][i][j][m][l]);
                                                    fourth[k][i].addTerm(1, w3[k - 1][i][j][m][l]);
                                                    fourth[k][i].addTerm(1, w3[k - 2][i][j][m][l]);
                                                } else if (k == 1) {
                                                    fourth[k][i].addTerm(1, w3[1][i][j][m][l]);
                                                    fourth[k][i].addTerm(1, w3[0][i][j][m][l]);
                                                    fourth[k][i].addTerm(1, w3[R + 1][i][j][m][l]);
                                                } else {
                                                    fourth[k][i].addTerm(1, w3[0][i][j][m][l]);
                                                    fourth[k][i].addTerm(1, w3[R + 1][i][j][m][l]);
                                                    fourth[k][i].addTerm(1, w3[R][i][j][m][l]);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for (int j = 0; j < N; j++) {
                    if (G.belongsG(i, j)) {
                        fourth[k][i].addTerm(1, w1[k][j][i]);
                        for (int m = 0; m < N; m++) {
                            if (m != j) {
                                if (G.belongsG(m, j)) {
                                    if (k >= 1) {
                                        fourth[k][i].addTerm(1, w2[k][j][i][m]);
                                        fourth[k][i].addTerm(1, w2[k - 1][j][m][i]);
                                    } else {
                                        fourth[k][i].addTerm(1, w2[0][j][i][m]);
                                        fourth[k][i].addTerm(1, w2[R + 1][j][m][i]);
                                    }
                                    for (int l = 0; l < N; l++) {
                                        if (m != j && m != l && l != j) {
                                            if (G.belongsG(l, j)) {
                                                if (k >= 2) {
                                                    fourth[k][i].addTerm(1, w3[k][j][i][m][l]);
                                                    fourth[k][i].addTerm(1, w3[k - 1][j][m][i][l]);
                                                    fourth[k][i].addTerm(1, w3[k - 2][j][m][l][i]);
                                                } else if (k == 1) {
                                                    fourth[k][i].addTerm(1, w3[1][j][i][m][l]);
                                                    fourth[k][i].addTerm(1, w3[0][j][m][i][l]);
                                                    fourth[k][i].addTerm(1, w3[R + 1][j][m][l][i]);
                                                } else {
                                                    fourth[k][i].addTerm(1, w3[0][j][i][m][l]);
                                                    fourth[k][i].addTerm(1, w3[R + 1][j][m][i][l]);
                                                    fourth[k][i].addTerm(1, w3[R][j][m][l][i]);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                cplex.addEq(fourth[k][i],1);
            }
        }

        //(5)
        IloLinearIntExpr[][] fifth = new IloLinearIntExpr[R-1][N];
        for (int k = 0; k < R-1; k++) {
            for (int i = 0; i < N; i++) {
                fifth[k][i]=cplex.linearIntExpr();
                for (int j = 0; j < N; j++) {
                    if (G.belongsG(j, i)) {
                        fifth[k][i].addTerm(1, w1[k][i][j]);
                        fifth[k][i].addTerm(1, w1[k+1][i][j]);
                        for (int m = 0; m < N; m++) {
                            if (m != j) {
                                if (G.belongsG(m, i)) {
                                    if(k>=1) {
                                        fifth[k][i].addTerm(1, w2[k - 1][i][j][m]);
                                        fifth[k][i].addTerm(1, w2[k][i][j][m]);
                                        fifth[k][i].addTerm(1, w2[k + 1][i][j][m]);
                                    }else{
                                        fifth[k][i].addTerm(1, w2[R+1][i][j][m]);
                                        fifth[k][i].addTerm(1, w2[0][i][j][m]);
                                        fifth[k][i].addTerm(1, w2[1][i][j][m]);
                                    }
                                    for (int l = 0; l < N; l++) {
                                        if (m != j && m != l && l != j) {
                                            if (G.belongsG(l, i)) {
                                                if (k >= 2) {
                                                    fifth[k][i].addTerm(1, w3[k-2][i][j][m][l]);
                                                    fifth[k][i].addTerm(1, w3[k-1][i][j][m][l]);
                                                    fifth[k][i].addTerm(1, w3[k][i][j][m][l]);
                                                    fifth[k][i].addTerm(1, w3[k+1][i][j][m][l]);
                                                }else if (k==1){
                                                    fifth[k][i].addTerm(1, w3[R+1][i][j][m][l]);
                                                    fifth[k][i].addTerm(1, w3[0][i][j][m][l]);
                                                    fifth[k][i].addTerm(1, w3[1][i][j][m][l]);
                                                    fifth[k][i].addTerm(1, w3[2][i][j][m][l]);
                                                }else {
                                                    fifth[k][i].addTerm(1, w3[R][i][j][m][l]);
                                                    fifth[k][i].addTerm(1, w3[R+1][i][j][m][l]);
                                                    fifth[k][i].addTerm(1, w3[0][i][j][m][l]);
                                                    fifth[k][i].addTerm(1, w3[1][i][j][m][l]);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                cplex.addLe(fifth[k][i],1);
            }
        }

        //(6)
        IloLinearIntExpr[][] sixth = new IloLinearIntExpr[R - 3][N];
        for (int k = 0; k < R-3; k++) {
            for (int i = 0; i < N; i++) {
                sixth[k][i]=cplex.linearIntExpr();
                for(int q=k;q<k+4;q++){
                    for (int j = 0; j < N; j++) {
                        if (G.belongsG(j, i)) {
                            sixth[k][i].addTerm(1,w1[q][i][j]);
                            for (int m = 0; m < N; m++) {
                                if (m != j) {
                                    if (G.belongsG(m, i)) {
                                        if(q>=1) {
                                            sixth[k][i].addTerm(1, w2[q - 1][i][j][m]);
                                            sixth[k][i].addTerm(1, w2[q][i][j][m]);
                                        }else{
                                            sixth[k][i].addTerm(1, w2[R+1][i][j][m]);
                                            sixth[k][i].addTerm(1, w2[0][i][j][m]);
                                        }
                                        for (int l = 0; l < N; l++) {
                                            if (m != j && m != l && l != j) {
                                                if (G.belongsG(l, i)) {
                                                    if (q >= 2) {
                                                        sixth[k][i].addTerm(1, w3[q-2][i][j][m][l]);
                                                        sixth[k][i].addTerm(1, w3[q-1][i][j][m][l]);
                                                        sixth[k][i].addTerm(1, w3[q][i][j][m][l]);
                                                    }else if (q==1){
                                                        sixth[k][i].addTerm(1, w3[R+1][i][j][m][l]);
                                                        sixth[k][i].addTerm(1, w3[0][i][j][m][l]);
                                                        sixth[k][i].addTerm(1, w3[1][i][j][m][l]);
                                                    }else {
                                                        sixth[k][i].addTerm(1, w3[R][i][j][m][l]);
                                                        sixth[k][i].addTerm(1, w3[R+1][i][j][m][l]);
                                                        sixth[k][i].addTerm(1, w3[0][i][j][m][l]);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                cplex.addGe(sixth[k][i],1);
            }
        }

        double time=cplex.getCplexTime();
        if (cplex.solve()) {
            double leadTime=cplex.getCplexTime()-time;
            double obj = cplex.getObjValue();
            int[][] schedule = new int[N][R];
            char[][] HAP = new char[N][R];

            for (int k = 0; k < R; k++) {
                for (int t = 0; t < N; t++) {
                    for (int i = 0; i < N; i++) {
                        if(t!=i) {
                            if (G.belongsG(i, t)) {
                                if (cplex.getValue(w1[k][t][i]) > 0.5) {
                                    schedule[t][k] = i;
                                    schedule[i][k] = t;
                                    HAP[t][k] = 'A';
                                    HAP[i][k] = 'H';
                                    if(k<R-1)
                                        HAP[t][k + 1] = 'H';
                                }
                                for (int j = 0; j < N; j++) {
                                    if (t != j && i != j) {
                                        if (G.belongsG(j, t)) {
                                            if (cplex.getValue(w2[k][t][i][j]) > 0.5) {
                                                schedule[t][k] = i;
                                                schedule[i][k] = t;
                                                schedule[j][k + 1] = t;
                                                schedule[t][k + 1] = j;
                                                HAP[t][k] = 'A';
                                                HAP[i][k] = 'H';
                                                HAP[t][k + 1] = 'A';
                                                HAP[j][k + 1] = 'H';
                                                if(k<R-2)
                                                    HAP[t][k + 2] = 'H';
                                            }

                                            for (int l = 0; l < N; l++) {
                                                if (l != j && l != i && l != t) {
                                                    if (G.belongsG(l, t)) {
                                                        if (cplex.getValue(w3[k][t][i][j][l]) > 0.5) {
                                                            schedule[t][k] = i;
                                                            schedule[i][k] = t;
                                                            schedule[j][k + 1] = t;
                                                            schedule[t][k + 1] = j;
                                                            schedule[l][k + 2] = t;
                                                            schedule[t][k + 2] = l;
                                                            HAP[t][k] = 'A';
                                                            HAP[i][k] = 'H';
                                                            HAP[t][k + 1] = 'A';
                                                            HAP[j][k + 1] = 'H';
                                                            HAP[t][k + 2] = 'A';
                                                            HAP[l][k + 2] = 'H';
                                                            if(k<R-3)
                                                                HAP[t][k + 3] = 'H';
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
/*            System.out.println();
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
            System.out.println();*/
            writeResult(saveResultRef2,schedule,HAP,obj,leadTime,model);
            cplex.end();
            return obj;
        }
        else {
            System.out.println("problem not solved");
            cplex.end();
            return -1;
        }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return -1;
    }
}