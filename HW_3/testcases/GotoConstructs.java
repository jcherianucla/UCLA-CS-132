class GotoConstructs {
    public static void main(String[] x) {
        int i;
        int MAX;
        int j;
        i = 0;
        MAX = 10;
        while(i < MAX){
            j = 0;
            if(i < 5){
                while(j < 3) {
                    System.out.println(i);
                    j = j + 1;
                }
            }
            else{
                System.out.println(i*i);
            }
            i = i + 1;
        }
    }
}
