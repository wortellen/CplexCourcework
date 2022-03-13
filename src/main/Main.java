package main;

import ilog.concert.IloException;
import models.Basic;
import config.Model;
import models.Reformulation1;
import config.GSet;
import models.Reformulation2;

import java.io.IOException;

import static config.Const.*;

public class Main {

    public static void main(String[] args) throws IOException, IloException {

        Model model = new Model(N);
        //model.setDistanceMatrixRandom();
        model.setDistanceMatrix(matrixPath);
        model.printDistanceMatrix();
        model.saveMatrix(matrixPath);

        Basic b = new Basic();
        b.start(model,1);
        GSet G = new GSet();

        Reformulation1 r1 =  new Reformulation1();
        r1.start(model,G,0);
        Reformulation2 r2 =  new Reformulation2();
        r2.start(model,G,0);
    }
}
