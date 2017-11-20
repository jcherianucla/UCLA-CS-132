class NullPointerMember {
    public static void main(String[] x) {
        int[] x;
        x = new int[5];
        x[5] = 100;
        System.out.println(x[3]);
    }
}

