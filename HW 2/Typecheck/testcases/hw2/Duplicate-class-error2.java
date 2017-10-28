class Main {
	public static void main(String[] a) {
		System.out.println(new B().run());
	}
}

class A extends B{
	int x;
	public int run() {
		x = 1;
		return x;
	}

}

class B {
	int y;
	public int run() {
		return y;
	}
}

class A extends C {
}

class C {

}
