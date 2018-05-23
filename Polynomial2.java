// Polynomial2.java CS5125/6025 Cheng 2017
// GF(2^8) and polynomials over the field
// Reed Solomon encoding of data entered from the command line (args[0])
// nine data bytes with 17 error correction bytes appended
// up to 8 random bytes are altered with random errors
// Berlekamp-Massey for location solving
// Forney for error solving ad error correction completion
// Usage: java Polynomial2 ninebytes

import java.util.*;

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
		for (int i = 0; i < fieldSize - 1; i++) log[alog[i]] = i;
  	}

	public int multiply(int a, int b){  // multiplication in GF
    		return (a == 0 || b == 0) ? 0 : alog[(log[a] + log[b]) % (fieldSize - 1)];
  	}

	public int multiplicativeInverse(int a){ // a^(-1) to define division in GF
		return alog[fieldSize - 1 - log[a]];
	}
};

public class Polynomial2{
	static GF f = new GF(256, 2, 0x11d);
	static int Gdegree = 17;
	static int maxNumberOfErrors = 8;
	int[] coeff = null;  
	// coeff[0] is the constant term, coeff[coeff.length - 1] is the highest power term
	static Polynomial2 G = makeRSG(Gdegree);

	public Polynomial2(int length){ coeff = new int[length]; } // constructor

	public Polynomial2(String data){ // turn string around as data polynomial
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
		if (coeff.length == 0){ 
			System.out.println(title + " [ ]");
			return;
		}
		System.out.print(title + " [ ");
		for (int i = coeff.length - 1; i > 0; i--) System.out.print(coeff[i] + " ");
		System.out.println(coeff[0] + " ]");
  	}

	public Polynomial2 scale(int a){ // ap(x)
		Polynomial2 newp = new Polynomial2(coeff.length);
		for (int i = 0; i < coeff.length; i++) 
			newp.coeff[i] = f.multiply(coeff[i], a);
     		return newp;
  	}

	public Polynomial2 shift(int r){ // x^r p(x)
		Polynomial2 newp = new Polynomial2(coeff.length + r);
		for (int i = 0; i < coeff.length; i++) newp.coeff[i + r] = coeff[i];
		for (int i = 0; i < r; i++) newp.coeff[i] = 0;
     		return newp;
  	}

	public Polynomial2 add(Polynomial2 p2){ // p(x) + p2(x)
		if (coeff.length >= p2.coeff.length){ 
			Polynomial2 newp = new Polynomial2(coeff.length);
			for (int i = 0; i < p2.coeff.length; i++) 
				newp.coeff[i] = coeff[i] ^ p2.coeff[i];
			for (int i = p2.coeff.length; i < coeff.length; i++) 
				newp.coeff[i] = coeff[i];
     				return newp;
		}else{
			Polynomial2 newp = new Polynomial2(p2.coeff.length);
			for (int i = 0; i < coeff.length; i++) 
				newp.coeff[i] = coeff[i] ^ p2.coeff[i];
			for (int i = coeff.length; i < p2.coeff.length; i++) 
				newp.coeff[i] = p2.coeff[i];
     				return newp;
		}
  	}

	public Polynomial2 RSencode(){ // shift, mod G and add remainder
		Polynomial2 tmp = shift(Gdegree);
		int head = tmp.coeff.length - 1;
		for (int i = tmp.coeff.length - G.coeff.length; i >= 0; i--)
			tmp = tmp.add(G.scale(tmp.coeff[head--]).shift(i)); 
		Polynomial2 ret = shift(Gdegree);
		for (int i = 0; i < Gdegree; i++) ret.coeff[i] = tmp.coeff[i];
		return ret;
	}

 	static public Polynomial2 makeRSG(int t){
		Polynomial2 G = new Polynomial2(2);
		G.coeff[0] = G.coeff[1] = 1;
		for (int i = 1; i < t; i++) G = G.shift(1).add(G.scale(f.alog[i]));
		return G;
	}

	HashMap<Integer, Integer> randomErrors(){  // used on result of RSencode
		// key is error position, value is error magnitude
		int numberOfCodewords = coeff.length;
		Random random = new Random();
		// number of errors may be from 0 to 8
		int numberOfErrors = random.nextInt(maxNumberOfErrors + 1);
		HashMap<Integer, Integer> errors = new HashMap<Integer, Integer>();
		while (errors.size() < numberOfErrors){ // random error positions
			int position = random.nextInt(numberOfCodewords);
			if (!errors.containsKey(position))
				errors.put(position, 1 + random.nextInt(f.fieldSize - 1));
		}
		return errors;
	}

	Polynomial2 addError(HashMap<Integer, Integer> errors){  
		// used on result of RSencode to get errorAdded
		// used on errorAdded to get result of RSencode
		int numberOfCodewords = coeff.length;
		Polynomial2 errorAdded = new Polynomial2(numberOfCodewords);
		for (int i = 0; i < numberOfCodewords; i++) 
			errorAdded.coeff[i] = coeff[i];
		errors.forEach((k,v)->{ errorAdded.coeff[k] ^= v; });
		return errorAdded;
	}

	Polynomial2 computeSyndrome(){  // used on errorAdded and returns syndrome
		Polynomial2 s = new Polynomial2(Gdegree);
		for (int i = 0; i < Gdegree; i++) s.coeff[i] = evaluate(f.alog[i]);
		return s;
	}

	Polynomial2 berlekampMassey(){  // used on syndrome and returns errorLocator
		Polynomial2 op = new Polynomial2(1); op.coeff[0] = 1;
		Polynomial2 ep = new Polynomial2(1); ep.coeff[0] = 1;
		Polynomial2 np = null;
		for (int i = 0; i < Gdegree; i++){
			ep.display(Integer.toString(i)); // optional
			op = op.shift(1);
			int d = coeff[i];
			for (int j = 1; j < ep.coeff.length; j++)
				d ^= f.multiply(ep.coeff[j], coeff[i - j]);
			if (d != 0){
				if (op.coeff.length > ep.coeff.length){
					np = op.scale(d);
					op = ep.scale(f.multiplicativeInverse(d));
					ep = np;
				}
				ep = ep.add(op.scale(d));
			}
		}
		return ep;
	}

	HashSet<Integer> findZeros(){  // returns zeros of an polynomial as a set
		HashSet<Integer> zeros = new HashSet<Integer>();
		for (int i = 0; i < f.fieldSize; i++) 
			if (evaluate(i) == 0) zeros.add(i);
		return zeros;
	}
		
	Polynomial2 computeZ(Polynomial2 locator){  // part of Forney, used on syndrome
		Polynomial2 Z = new Polynomial2(locator.coeff.length);
		for (int i = 0; i < locator.coeff.length; i++){
			Z.coeff[i] = locator.coeff[i];
			for (int j = i + 1; j < locator.coeff.length; j++)
				Z.coeff[i] ^= f.multiply(locator.coeff[j] , coeff[j - i]);
		}
		return Z;
	}

    HashMap<Integer, Integer> forney(HashSet<Integer> betaInverses){ // used on Z
        HashMap<Integer, Integer> errors = new HashMap<Integer, Integer>();
        HashSet<Integer> betas = new HashSet<Integer>();
        for (int p: betaInverses) betas.add(f.multiplicativeInverse(p));
        for (int p: betaInverses){ // for each beta_i^-1 as p
            int d = 1; // d is for denominator in Forney
            int q = f.multiplicativeInverse(p); // q is beta_i
            for (int b: betas) if (q != b) // b is beta_k with k != i
                d = f.multiply(f.multiply(1^b,p),d);
                // d * (1 + beta_k * beta_i^-1)
                // use f.multiply for * and ^ for +
            int e = f.multiply(evaluate(p),f.multiplicativeInverse(d));
            // Z(beta_i^-1) / d
                // use evaluate() for Z()
                // use f.multiply(a, f.multiplicativeInverse(b)) for a/b
                errors.put(f.log[q], e);
        }
        return errors;
    }
			

	void reedsolomon(){  // an experiment of Reed-Solomom encoding and decoding
		Polynomial2 encoded = RSencode();  // append 17 error correction bytes
		encoded.display("encoded");  
		HashMap<Integer, Integer> unknownErrors = encoded.randomErrors();
		Polynomial2 errorAdded = encoded.addError(unknownErrors);
		errorAdded.display("errorAdded"); 
		Polynomial2 syndrome = errorAdded.computeSyndrome();
		syndrome.display("syndrome"); // optional
		Polynomial2 errorLocator = syndrome.berlekampMassey();
		errorLocator.display("errorLocator"); // optional
		Polynomial2 Z = syndrome.computeZ(errorLocator);
		HashSet<Integer> betaInverses = errorLocator.findZeros();
		HashMap<Integer, Integer> recoveredErrors = Z.forney(betaInverses);
		Polynomial2 corrected = errorAdded.addError(recoveredErrors);
		//recoveredCode.display("corrected");
	}

	public static void main(String[] args){
		Polynomial2 p = new Polynomial2(args[0]);
		p.reedsolomon();
	}
}

	
