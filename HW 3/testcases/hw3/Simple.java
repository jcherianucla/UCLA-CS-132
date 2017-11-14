class Main {
	public static void main(String[] a){
	    int x;
	    x = 5;
	    if (x < 7) {
	        if (x < 3) {
				System.out.println(0);
				if (x < 4) {
					x = 8;
					System.out.println(1);
				} else {
					x = 110;
					System.out.println(2);
				}
			} else {
	        	System.out.println(3);
			}
		} else {
	    	System.out.println(4);
		}
		if (x < 1) {
	    	x = 9;
		} else {
	    	x = 10;
		}
	}
}