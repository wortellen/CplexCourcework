package optimizer;

import config.Const;
import config.GSet;
import config.Model;

import models.Reformulation2;

import java.io.IOException;
import java.util.Random;

import static config.Const.N;
import static config.Const.matrixPath;

public class Greedy {
    GSet gset;
    int G = N*(N-1)/2;
    Model model;
    Reformulation2 r2 =  new Reformulation2();

    public Greedy(GSet gset, Model model) {
        this.gset = gset;
        this.model = model;
    }

    public void start(int steps){
        Random random = new Random();
        GSet tmp=gset.clone();
        boolean newMin=false;
        double minDist=calculate(gset);
        System.out.println("======================================  "+minDist);
        for (int i=0;i<steps;i++){
            //step
            System.out.println("======================================  Step: "+i+" cur dist: "+minDist);
            for (int j = 0;j<G;j++){
                GSet.Pair truePair = new GSet.Pair(tmp.pairs[j].i,tmp.pairs[j].j);
                GSet.Pair pair = new GSet.Pair(tmp.pairs[j].j,tmp.pairs[j].i);
                tmp.pairs[j]=pair;
                double tmpSum = calculate(tmp);
                if(tmpSum<minDist && tmpSum!=-1){
                    newMin=true;
                    minDist=tmpSum;
                    gset=tmp.clone();
                    System.out.println("======================================  New dist: "+minDist);
                }
                tmp.pairs[j]=truePair;
            }
            tmp=gset.clone();
            if(!newMin){
                int k = Math.abs(random.nextInt()%G);
                GSet.Pair pair = new GSet.Pair(tmp.pairs[k].j,tmp.pairs[k].i);
                tmp.pairs[k]=pair;
            }
            newMin=false;
        }


    }
    public double calculate(GSet set){
        return r2.start(model,set,0);
    }

    public static void main(String[] args) {
        Model model = new Model(N);
        //model.setDistanceMatrixRandom();
        model.setDistanceMatrix(matrixPath);
        model.printDistanceMatrix();

        GSet mySet= new GSet();
        Greedy g = new Greedy(mySet,model);
        g.start(100);
    }
}
