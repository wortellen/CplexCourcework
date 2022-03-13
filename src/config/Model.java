package config;

import java.io.*;
import java.util.Random;

import static config.Const.N;
import static config.Const.R;

public class Model {
    public int[][] distanceMatrix;
    private final int size;
    public String[][] schedule;

    public Model(int size) {
        this.size = size;
    }

    public void setDistanceMatrixRandom(){
        distanceMatrix = new int[size][size];
        Random random = new Random();
        for(int i=0;i<distanceMatrix.length;i++){
            for(int j=i;j<distanceMatrix.length;j++){
                if(i==j)
                    distanceMatrix[i][j]=0;
                else {
                    distanceMatrix[i][j] = 50 + random.nextInt(2000);
                    distanceMatrix[j][i] = distanceMatrix[i][j];
                }
            }
        }
    }
    public void setDistanceMatrix(String path){
        File file = new File(path);
        try {
            FileReader fr = new FileReader(file);
            BufferedReader bf = new BufferedReader(fr);
            String line =bf.readLine();
            int size = Integer.parseInt(line);
            distanceMatrix = new int[size][size];
            int count = 0;
            while(line!=null){
                line = bf.readLine();
                if(line!=null) {
                    String[] tmp = line.split(" ");
                    for (int i = 0; i < size; i++) {
                        distanceMatrix[count][i] = Integer.parseInt(tmp[i]);
                    }
                    count++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void printDistanceMatrix(){
        for(int i=0;i<distanceMatrix.length;i++){
            for(int j=0;j<distanceMatrix.length;j++) {
                System.out.printf( "%3d",distanceMatrix[i][j]);
                System.out.print( " ");
            }
            System.out.println();
        }
    }
    public void saveMatrix(String path) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.write(distanceMatrix.length+"\n");
        for (int[] matrix : distanceMatrix) {
            StringBuilder str = new StringBuilder();
            for (int j = 0; j < distanceMatrix.length; j++) {
                str.append(matrix[j]).append(' ');
            }
            fw.write(str.substring(0, str.length() - 1));
            fw.append("\n");
        }
        fw.flush();
    }
    public static void printOpponents(int[][] schedule){
        for(int i=0;i<N;i++) {
            System.out.print("Team "+(i+1)+": ");
            for (int j = 0; j < R; j++) {
                System.out.print(schedule[i][j]+1);
                System.out.print(" ");
            }
            System.out.println();
        }
    }
    public static void printHAP(char[][] HAP){
        for(int i=0;i<N;i++) {
            System.out.print("Team "+(i+1)+": ");
            for (int j = 0; j < R; j++) {
                System.out.print(HAP[i][j]);
                System.out.print(" ");
            }
            System.out.println();
        }
    }
    public static void checkDistance(int[][] schedule, char[][] HAP, Model m){
        int sum=0;
        int tmp=-1;
        for(int i=0 ;i<N;i++){
            for(int j=0 ;j<R;j++){
                if(HAP[i][j]=='H'){
                    if(tmp!=-1)
                        sum+=m.distanceMatrix[tmp][i];
                    tmp=i;
                }
                else{
                    if(tmp!=-1)
                        sum+=m.distanceMatrix[tmp][schedule[i][j]];
                    else sum+=m.distanceMatrix[i][schedule[i][j]];
                    tmp=schedule[i][j];
                }
            }
            tmp=-1;
        }
        for(int i=0 ;i<N;i++){
            if(HAP[i][R-1]=='A'){
                sum+=m.distanceMatrix[i][schedule[i][R-1]];
            }
        }
        System.out.print(sum);
    }
    public static boolean isValidSchedule(int[][] schedule){
        int sum = 0;
        for (int j = 0;j<schedule.length;j++){
            sum+=j;
        }
        int tmp=0;
        for (int i = 0;i<schedule.length;i++) {
            for (int j = 0; j < schedule[1].length; j++) {
                if (i == schedule[i][j])
                    return false;
                tmp += schedule[i][j];
            }
            if (tmp != sum - i)
                return false;
            tmp=0;
        }
        return true;
    }
    public static boolean isValidHAP(char[][] HAP){
        int countH = 0;
        int countA = 0;
        for (char[] chars : HAP) {
            for (int j = 0; j < HAP[1].length; j++) {
                if (chars[j] == 'H') {
                    countH++;
                    countA = 0;
                }
                if (chars[j] == 'A') {
                    countA++;
                    countH = 0;
                }
                if (countA > 3 || countH > 3)
                    return false;
            }
            countA = 0;
            countH = 0;
        }
        return true;
    }
    public static void writeResult(String path, int[][] schedule, char[][] HAP, double obj, double leadTime, Model model)   {
        try {
            FileWriter fw = new FileWriter(path);

        fw.write("Number of teams: "+N+"\n");
        fw.write("Number of rounds: "+R+"\n");
        fw.append("\n");
        fw.write("Distance matrix: \n");
        for (int[] matrix : model.distanceMatrix) {
            StringBuilder str = new StringBuilder();
            for (int j = 0; j < model.distanceMatrix.length; j++) {
                str.append(matrix[j]).append(' ');
            }
            fw.write(str.substring(0, str.length() - 1));
            fw.append("\n");
        }
        fw.append("\n");
        fw.write("Lead time = "+leadTime);
        fw.append("\n");
        fw.write("Objective = "+obj);
        fw.append("\n");
        fw.append("\n");
        fw.write("Opponent schedule: \n");
        for(int i=0;i<N;i++) {
            fw.write("Team "+(i+1)+": ");
            for (int j = 0; j < R; j++) {
                fw.write(Integer.toString(schedule[i][j]+1));
                fw.write(" ");
            }
            fw.append("\n");
        }
        fw.append("\n");
        fw.write("HAP: \n");
        for(int i=0;i<N;i++) {
            fw.write("Team "+(i+1)+": ");
            for (int j = 0; j < R; j++) {
                fw.write(HAP[i][j]);
                fw.write(" ");
            }
            fw.append("\n");
        }
        fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
