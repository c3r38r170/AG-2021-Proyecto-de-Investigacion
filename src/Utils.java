public class Utils {
	
	static int randomIntBetween(int a,int b){
		return (int)Math.round(Math.random()*((b-a)+1)+(a-.5));
	}

		static double randomDoubleBetween(double a,double b){
		return Math.random()*((b-a)+1)+(a-.5);
	}
	
}
