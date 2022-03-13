package config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import static config.Const.N;
import static config.Const.gPath;

public class GSet  implements Cloneable{
    public Pair[] pairs = new Pair[N * (N - 1) / 2];

    public GSet() {
        File file = new File(gPath);
        try {
            FileReader fr = new FileReader(file);
            BufferedReader bf = new BufferedReader(fr);
            String line = " ";
            int count = 0;
            while (line != null) {
                line = bf.readLine();
                if (line != null) {
                    String[] tmp = line.split(" ");
                    pairs[count] = new Pair(Integer.parseInt(tmp[0]),Integer.parseInt(tmp[1]));
                    count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public GSet(GSet copy){
        this.pairs= copy.pairs;
    }
    public boolean belongsG(int i, int j) {
        for (Pair gSet : pairs) {
            if (gSet.i == i && gSet.j == j) {
                return true;
            }
        }
        return false;
    }
    public int length(){
       return pairs.length;
    }

    @Override
    public GSet clone() {
        try {
            GSet clone = (GSet) super.clone();
            Pair[] pairs = new Pair[this.pairs.length];
            for(int i=0;i<this.pairs.length;i++){
                pairs[i]=this.pairs[i].clone();
            }
            clone.pairs=pairs;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public static class Pair implements Cloneable{
        public int i;
        public int j;


        public Pair(int i, int j) {
            this.i = i;
            this.j = j;
        }

        @Override
        public Pair clone() {
            try {
                Pair clone = (Pair) super.clone();
                clone.i=this.i;
                clone.j=this.j;
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }
    public void setRandom(){

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Pair pair : pairs) {
            sb.append(pair.i);
            sb.append("-");
            sb.append(pair.j);
            sb.append("\n");
        }
        return sb.toString();
    }
}
