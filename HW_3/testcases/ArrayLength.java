class NullPointerMember {
    public static void main(String[] x) {
        int[] x;
        int y;
        x = new int[5];
        y = x.length;
        System.out.println(y);
    }
}
