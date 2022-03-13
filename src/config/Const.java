package config;

public class Const {
    public static final int N = 8;
    public static final int R = N - 1;
    public static final int L = 1;
    public static final int U = 3;
    public static int resCount=0;
    public static final String matrixPath = "Config/Matrix_test.txt";
    public static final String gPath = "Config/G.txt";
    public static final String saveResultBasic = String.format("Results/Basic/basic_%d_%d.txt",N,resCount);
    public static final String saveResultRef1 = String.format("Results/Ref1/ref1_%d_%d.txt",N,resCount);
    public static final String saveResultRef2 = String.format("Results/Ref2/ref2_%d_%d.txt",N,resCount);


}