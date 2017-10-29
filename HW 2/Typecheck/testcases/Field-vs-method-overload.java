class Main {
	public static void main(String[] a) {
		System.out.println(new A().foo());
	}
}
class A{
	public int foo(){
		return 5;
	}
}

class B extends A{
	int foo;
}
