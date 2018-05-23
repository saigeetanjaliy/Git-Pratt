// DiffieHellmanC.java CS5125/6025 Cheng 2017
// checking primality of Group 5 q and (q-1)/2
// checking that 2 is a primitive element in GF(q) (a primitive root of q)
// Usage: java DiffieHellmanC

import java.math.*;
import java.io.*;
import java.util.*;

public class DiffieHellmanC{
  String hexQ = null;
  BigInteger q;
  BigInteger p = null;  // p = (q-1)/ 2
  static BigInteger two = new BigInteger("2");
  static BigInteger one = new BigInteger("1");
  void readQ(String filename){
    Scanner in = null;
    try {
     in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
    hexQ = in.nextLine();
    in.close();
    q = new BigInteger(hexQ, 16);
  }

 void testPrimality(){
   if (q.isProbablePrime(200)) 
    System.out.println("q is probably prime");
   q = q.subtract(one);
   p = q.divide(two);
   if (p.isProbablePrime(200)) 
    System.out.println("p is probably prime");
 }

 void testPrimitiveness(){
   BigInteger twopq = two.modPow(p,q);
   System.out.println(twopq.toString(16));
 }

 public static void main(String[] args){
   DiffieHellmanC dh = new DiffieHellmanC();
   dh.readQ("DHgroup5.txt");
   dh.testPrimality();
   dh.testPrimitiveness();
 }
}

   