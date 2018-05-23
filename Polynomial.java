// Polynomial.java CS5125/6025 Cheng 2017
// GF(2^8) and polynomials over the field
// Reed Solomon encoding of data entered from the command line (args[0]
// Usage: java Polynomial

class GF{  // finite field of 2^k elements so that addition and subtractions are XOR
	int fieldSize = 0;  // 2^k
	public int logBase = 0;  // a primative element
	int irreducible = 0;  // a irreducible polynomial of degree k
	public int[] alog = null;  // all powers of the logBase
	public int[] log = null;  // discrete log, log[0] is not defined

	public GF(int size, int base, int irr){  // constructor
		fieldSize = size; logBase = base; irreducible = irr; 
		alog = new int[fieldSize]; log = new int[fieldSize];
		makeLog();
	}

	int modMultiply(int a, int b, int m){  // multiply based on XOR as addition
		int product = 0;
		for (; b > 0; b >>= 1){
			if ((b & 1) > 0) product ^= a;
			a <<= 1;
			if ((a & fieldSize) > 0) a ^= m;
    		}
		return product;
	}    

	void makeLog(){  // first make all powers and then discrete log
		alog[0] = 1;
		for (int i = 1; i < fieldSize; i++)
		alog[i] = modMultiply(logBase, alog[i - 1], irreducible);
		for (int i = 1; i < fieldSize; i++) log[alog[i]] = i;
  	}

	public int multiply(int a, int b){  // multiplication in GF
    		return (a == 0 || b == 0) ? 0 : alog[(log[a] + log[b]) % (fieldSize - 1)];
  	}

	public int multiplicativeInverse(int a){ // a^(-1) to define division in GF
		return alog[fieldSize - 1 - log[a]];
	}
};

public class Polynomial{
	static GF f = new GF(256, 2, 0x11d);
	static int Gdegree = 17;
	int[] coeff = null;  
	// coeff[0] is the constant term, coeff[coeff.length - 1] is the highest power term
	static Polynomial G = makeRSG(Gdegree);

	public Polynomial(int length){ coeff = new int[length]; }

	public Polynomial(String data){ // turn string around as data polynomial
		coeff = new int[data.length()]; 
		for (int i = 0; i < coeff.length; i++) 
			coeff[coeff.length - 1 - i] = data.charAt(i);
	} 

	public int evaluate(int x){  // Horner's algorithm
   		int sum = coeff[coeff.length - 1];
		for (int i = coeff.length - 2; i >= 0; i--) 
			sum = f.multiply(sum, x) ^ coeff[i];
		return sum;
 	} 	

	public void display(String title) { // display with highest power first
		System.out.print(title + " [ ");
		for (int i = coeff.length - 1; i > 0; i--) System.out.print(coeff[i] + " ");
		System.out.println(coeff[0] + " ]");
  	}

	void arrayDisplay(int[] array){
		System.out.print(" [ ");
		for (int i = array.length - 1; i > 0; i--) System.out.print(array[i] + " ");
		System.out.println(array[0] + " ]");
	}

	public Polynomial scale(int a){ // ap(x)
		Polynomial newp = new Polynomial(coeff.length);
		for (int i = 0; i < coeff.length; i++) 
			newp.coeff[i] = f.multiply(coeff[i], a);
     		return newp;
  	}

	public Polynomial shift(int r){ // x^r p(x)
		Polynomial newp = new Polynomial(coeff.length + r);
		for (int i = 0; i < coeff.length; i++) newp.coeff[i + r] = coeff[i];
		for (int i = 0; i < r; i++) newp.coeff[i] = 0;
     		return newp;
  	}

	public Polynomial add(Polynomial p2){ // p(x) + p2(x)
		if (coeff.length >= p2.coeff.length){ 
			Polynomial newp = new Polynomial(coeff.length);
			for (int i = 0; i < p2.coeff.length; i++) 
				newp.coeff[i] = coeff[i] ^ p2.coeff[i];
			for (int i = p2.coeff.length; i < coeff.length; i++) 
				newp.coeff[i] = coeff[i];
     				return newp;
		}else{
			Polynomial newp = new Polynomial(p2.coeff.length);
			for (int i = 0; i < coeff.length; i++) 
				newp.coeff[i] = coeff[i] ^ p2.coeff[i];
			for (int i = coeff.length; i < p2.coeff.length; i++) 
				newp.coeff[i] = p2.coeff[i];
     				return newp;
		}
  	}

	public Polynomial RSencode(){ // shift, mod G and add remainder
		Polynomial tmp = shift(Gdegree);
		int head = tmp.coeff.length - 1;
		for (int i = tmp.coeff.length - G.coeff.length; i >= 0; i--)
			tmp = tmp.add(G.scale(tmp.coeff[head--]).shift(i)); 
		Polynomial ret = shift(Gdegree);
		for (int i = 0; i < Gdegree; i++) ret.coeff[i] = tmp.coeff[i];
		return ret;
	}

 	static public Polynomial makeRSG(int t){
		Polynomial G = new Polynomial(2);
		G.coeff[0] = G.coeff[1] = 1;
		for (int i = 1; i < t; i++) G = G.shift(1).add(G.scale(f.alog[i]));
		return G;
	}

	public static void main(String[] args){
		Polynomial p = new Polynomial(args[0]);
		p.RSencode().display("RScode");
		p.evaluate(17);
		Polynomial p1 = new Polynomial(17);
		p1.RSencode().display("RScode1");
		p1.evaluate(17);
	}
}

	
