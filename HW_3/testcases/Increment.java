class Increment{
    public static void main(String[] x){
        A obj;
        int[] arr;
        int i;
        obj = new A();
        arr = obj.constructor();
        arr = obj.increment();
        arr[0] = 1;
        arr[1] = 9;
        arr[2] = 2;
        arr[3] = 8;
        arr[4] = 3;
        arr[5] = 7;
        arr[6] = 4;
        arr[7] = 6;
        arr[8] = 5;
        arr[9] = 5;
        arr = obj.increment();
    }
}

class B{
    int[] x;
}

class A extends B{
    public int[] constructor(){
        x = new int[10];
        x[0] = 10;
        x[1] = 9;
        x[2] = 8;
        x[3] = 7;
        x[4] = 6;
        x[5] = 5;
        x[6] = 4;
        x[7] = 3;
        x[8] = 2;
        x[9] = 1;
        return x;
    }
    public int[] increment(){
        int i;
        i = 0;
        while(i < 10) {
            System.out.println(x[i]);
            i = i + 1;
        }
        return x;
    }
}
