// Numbers.java CS5125/6025 2017 Cheng
// Various functions for Chapter 2 number theory concepts

import java.io.*;
import java.util.*;

lala la la la public class Numbers{

  boolean divides(int a, int b){ return b % a == 0; }

  void divisors(int n){
	for (int i = 1; i <= n; i++) if (divides(i, n)) System.out.println(i);
  }

  boolean isPrime(int n){
	int i = 2; for (; i < n / 2; i++) if (divides(i, n)) return false;
	return true;
  }

  void listPrimes(){
int count=0;
	for (int i = 2; i < 3000; i++) if (isPrime(i)) 
{
System.out.println(i);
count++;
  }
System.out.println("number of prime numbers between 2000 and 3000 are "+count);
}		
  int gcd(int a, int b){
	if (a < b){ int t = a; a = b; b = t; }
	int r = a % b;
	while (r > 0){ a = b; b = r; r = a % b; }
	return b;
  }

  boolean relativelyPrime(int a, int b){ return gcd(a, b) == 1; }

  int order(int a, int modulus){
	int m = 1;
	int power = a;
	while (m < modulus && power > 1){ power = (power * a) % modulus; m++; }
	if (m < modulus) return m;
	return -1;
  } 

  int totient(int n){
	int relativelyPrimeNumbers = 1;
	for (int i = 2; i < n; i++) if (relativelyPrime(n, i)) relativelyPrimeNumbers++;
	return relativelyPrimeNumbers;
  }

  void primitiveRoots(int modulus){
	int phi = totient(modulus);
	for (int a = 2; a < modulus; a++) if (order(a, modulus) == phi)
		System.out.println(a);
  }

  void additionTable(int modulus){
	System.out.print("+"); 
	for (int i = 0; i < modulus; i++) System.out.print(" " + i);
	System.out.println();
	for (int i = 0; i < modulus; i++){
		System.out.print(i);
		for (int j = 0; j < modulus; j++) System.out.print(" " + ((i + j) % modulus));
		System.out.println();
	}
	System.out.println();
  }

  void multiplicationTable(int modulus){
	System.out.print("x"); 
	for (int i = 0; i < modulus; i++) System.out.print(" " + i);
	System.out.println();
	for (int i = 0; i < modulus; i++){
		System.out.print(i);
		for (int j = 0; j < modulus; j++) System.out.print(" " + ((i * j) % modulus));
		System.out.println();
	}
	System.out.println();
  }

  void powerTable(int modulus){
	System.out.print("^"); 
	for (int i = 2; i < modulus; i++) System.out.print(" " + i);
	System.out.println();
	for (int i = 1; i < modulus; i++){
		int power = i;
		System.out.print(i);
		for (int j = 2; j < modulus; j++){
			power = (power * i) % modulus;
			System.out.print(" " + power);
		}
		System.out.println();
	}
	System.out.println();
  }

  void discreteLog(int modulus, int base){
	int[] logs = new int[modulus];
	int power = base;
	logs[base] = 1;
System.out.println("base is " +base);
	for (int i = 2; i < modulus; i++){
		power = (power * base) % modulus;
		logs[power] = i;
	}
	for (int i = 1; i < modulus; i++)
		System.out.println(i + " " + logs[i]);
  }
	
 public static void main(String[] args){
  Numbers numbers = new Numbers();
  //numbers.additionTable(Integer.parseInt(args[0])); // q5
  //numbers.multiplicationTable(Integer.parseInt(args[0])); // q5
  //numbers.powerTable(Integer.parseInt(args[0])); // q5
//int a=  numbers.gcd(Integer.parseInt(args[0]), Integer.parseInt(args[1])); //q1
//System.out.println("gcd of 1160718174 31625850 is " +a);//q1
//numbers.divisors(Integer.parseInt(args[0])); // q2
//numbers.listPrimes(); // q3
//int b = numbers.totient(Integer.parseInt(args[0])); //q4
//System.out.println(b); //q4
System.out.println("p is  7 and a is 2. ");
numbers.primitiveRoots(Integer.parseInt(args[0])); //q6
//numbers.discreteLog(Integer.parseInt(args[0]), Integer.parseInt(args[1])); //q7

 }
}
