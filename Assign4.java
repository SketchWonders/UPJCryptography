/*
* RSA Envelope with a Vigenere cipher to encrypt and decrypt files
*/
import java.math.BigInteger;
import java.util.Scanner;
import java.util.Random;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Math;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.io.FileInputStream;
import java.io.ObjectInputStream;


public class Assign4{
	
	public static void main (String [] args){
		Scanner in = new Scanner (System.in);
		String fileNameIn = "";
		String fileNameOut = "";
		int choice = 5;
		while (choice !=4){
			System.out.println("What would you like to do. 1. make keys. 2. encrypt. 3. decrypt. 4 quit.");
			choice = in.nextInt();
			switch(choice){
				case 1:	makeKeys(); break;
				case 2: 
					System.out.print("To encrypt: ");
					fileNameIn = getFileName();
					System.out.print("Where would you like to save your encryption? ");
					fileNameOut = getFileName();
					encrypt(fileNameIn, fileNameOut); break;
				case 3: 
					System.out.print("To decrypt: ");
					fileNameIn = getFileName();
					System.out.print("Where would you like to save your decrypted message? ");
					fileNameOut = getFileName();
					decrypt(fileNameIn, fileNameOut); break;
				case 4: break;
				default: System.out.println("Invalid input.");
			}
		}	
	}
	
	public static String getFileName(){
		Scanner fn = new Scanner (System.in);
		String fileName = "";
		System.out.println("Please input a file name in the form \"name.txt\": ");
		fileName = fn.nextLine();
		//any check here?
		return fileName;
	}
	
	/*
	* Will generate values for p, s, n for RSA encryption
	* print p, s, N. Store p,N in pubkey.txt and store p,N in privkey.txt
	*/
	public static void makeKeys(){
		Random rnd = new Random();
		int bitLength = 500; //500;
		
		BigInteger valOne = BigInteger.valueOf(1);
		BigInteger x= BigInteger.probablePrime(bitLength, rnd);
		//System.out.println("x: " + x);
		BigInteger y = BigInteger.probablePrime(bitLength, rnd);
		//System.out.println("y: " + y);
		
		BigInteger n = y.multiply(x);
		System.out.println("N: " + n);
		BigInteger phi = (y.subtract(valOne)).multiply(x.subtract(valOne));
		//System.out.println("PHI: " + phi);
		
		BigInteger p = BigInteger.probablePrime(bitLength, rnd);
		System.out.println("p: " + p);
		
		BigInteger s = p.modInverse(phi);
		while(s.compareTo(valOne)!=1){
			//System.out.println("In while: ");
			s = s.add(phi);
		}
		System.out.println("s: " + s);
		
		
		//System.out.println("Does s work? " + ((s.multiply(p)).mod(phi)).equals(valOne));
		
		
		try{
			ObjectOutputStream outObject = new ObjectOutputStream(new FileOutputStream("pubkey.dat"));
			outObject.writeObject (n);
			outObject.writeObject (p);
			outObject.close();
		}catch(Exception e){
			System.out.println("Whelp... It didn't save the object1");
		}
		
		try{
			ObjectOutputStream outObject2 = new ObjectOutputStream(new FileOutputStream("privkey.dat"));
			outObject2.writeObject (n);
			outObject2.writeObject (s);
			outObject2.close();
		}catch(Exception e){
			System.out.println("Whelp... It didn't save the object2");
		}		
	}
	
	/*
	*/
	public static void encrypt(String fileIn, String fileOut){
		//read RSA from pubKey.txt
		//Generate random Vigenere cipher of 32 byte values.
		byte [] vigenereCipher = new byte [32];
		byte inputVal;
		int valToDo;
		
		for(int i = 0; i< vigenereCipher.length; i++){
			valToDo = (int)(Math.random()*(127+1));
			inputVal = (byte)valToDo;
			vigenereCipher[i] = inputVal;
		}
		
		//to check
		/*
		for (int i = 0; i< vigenereCipher.length; i++){
			System.out.println(i + ": " +(int)vigenereCipher[i]);
		}
		*/
		BigInteger vCipher;
		BigInteger x = BigInteger.valueOf(128);
		vCipher = x.multiply(BigInteger.valueOf((int)vigenereCipher[0]));
		for(int i = 1; i< vigenereCipher.length-1; i++){
			vCipher =x.multiply((BigInteger.valueOf((int)vigenereCipher[i])).add(vCipher));
		}
		vCipher = vCipher.add(BigInteger.valueOf((int)vigenereCipher[vigenereCipher.length-1]));
		
		//Read in objects to get key to store the cipher
		BigInteger n;
		BigInteger p;
		BigInteger cipherTXT;
		try{
			ObjectInputStream inObject = new ObjectInputStream(new FileInputStream("pubkey.dat"));
			n= (BigInteger)inObject.readObject ();
			p = (BigInteger)inObject.readObject ();
			inObject.close();
			cipherTXT = vCipher.modPow(p, n);
			try{
				ObjectOutputStream outObject = new ObjectOutputStream(new FileOutputStream("Big.dat"));
				outObject.writeObject (cipherTXT);
				outObject.close();
			}catch(Exception e){
				System.out.println("Whelp... It didn't save the object BIG");
			}
		}catch(Exception e){
			System.out.println("Whelp... It didn't save the object2");
		}
		
		//read in file to encrypt
		String message="";
		try{
			File file = new File (fileIn);
			Scanner sc = new Scanner(file);
			String incommingLine = "";
			while (sc.hasNextLine()){
				incommingLine = sc.nextLine();
				message = message +incommingLine + "\n";
			}
			//System.out.println ("Message to encrypt: " +message);
		}
		catch (FileNotFoundException e){
			System.out.println("That didn't work");
		}
		String temp = "";
		byte nextCode;
		//System.out.println("Per character: ");
		try(PrintWriter pout = new PrintWriter(new FileOutputStream(fileOut, false))){
			for (int i = 0; i< message.length(); i++){
				for (int j = 0; j< vigenereCipher.length; j++){
					//System.out.print("I: " + i + " J: " + j+" Message: " + message.charAt(i));
					temp = temp+ (char)((((message.charAt(i))+(int)vigenereCipher[j])%128));
					nextCode = (byte)(((message.charAt(i))+(int)vigenereCipher[j])%128);
					if(nextCode <0){
						nextCode = (byte)(nextCode +128);
					}
					//System.out.println(" Code: " + (char)nextCode);
					pout.println(nextCode);
					
					if(j!=vigenereCipher.length-1){
						i++;
					}
					//so that it won't keep going through rest of cipher array
					if (i>= message.length()){
						break;
					}
				}
			}
			System.out.println("=================================================");
			System.out.println("Encrypted message: "+temp);
			System.out.println("=================================================");
			System.out.println("Message encrypted.");
			pout.close();
		}
		catch (FileNotFoundException e){
			System.out.println("File does not exist");
		}
		
	}
	
	/*
	*/
	public static void decrypt(String fileIn, String fileOut){
		//read in key from big.dat
		BigInteger x = BigInteger.valueOf(128);
		BigInteger vigenereCipherKey = null;
		byte [] vigenereCipher = new byte [32];
		try{
			ObjectInputStream inObject = new ObjectInputStream(new FileInputStream("Big.dat"));
			vigenereCipherKey= (BigInteger)inObject.readObject ();
			inObject.close();
		}catch(Exception e){
			System.out.println("Whelp... It didn't save the object2");
		}
		
		//get private key 
		BigInteger n =null;
		BigInteger s = null;
		try{
			ObjectInputStream inObject = new ObjectInputStream(new FileInputStream("privkey.dat"));
			n= (BigInteger)inObject.readObject ();
			s = (BigInteger)inObject.readObject ();
			inObject.close();
		}catch(Exception e){
			System.out.println("Whelp... It didn't save the object2");
		}
		//decrypt key
		BigInteger actualKey = vigenereCipherKey.modPow(s, n);
		
		BigInteger remain = actualKey;
		BigInteger prev = remain.mod(x);
		/*
		for (int i = 0; i < vigenereCipher.length; i++){
			vigenereCipher[vigenereCipher.length-1-i] =(byte) (remain.mod(x)).intValue();
			remain = (remain.subtract((BigInteger.valueOf((int)vigenereCipher[i])))).divide(x);
		}
		*/
		byte temp [] = new byte [32];
		for(int i = 0; i< temp.length; i++){
			temp[i] = (byte) (remain.mod(x)).intValue();
			//System.out.print((int)temp[i]);
			remain = (remain.subtract((BigInteger.valueOf((int)temp[i])))).divide(x);
		}
		for(int i = 0; i< temp.length; i++){
			vigenereCipher[i] = temp[temp.length - 1 - i];
		}
		//to check
		/*
		for (int i = 0; i< vigenereCipher.length; i++){
			System.out.println(i + ": " + (int)vigenereCipher[i]);
		}
		*/
		int codeValue;
		String decryptedMessage = "";
		String messageIn = "";
		try{
			File file = new File (fileIn);
			Scanner sc = new Scanner(file);
			//String incommingLine = "";
			int i = 0;
			//System.out.println("Print per character: ");
			while (sc.hasNextLine()){
				try{
					codeValue = sc.nextInt();
					//System.out.print("Code: " + (char)codeValue);
					messageIn = messageIn+(char) codeValue;
			
					//codeValue = Integer.toString(incommingLine);
					codeValue = (codeValue -(int) vigenereCipher[i]) %128;
					if(codeValue <0){
						codeValue = codeValue+128;
					}
					//System.out.println(" Translated :"+(char)codeValue);
					
					
					decryptedMessage = decryptedMessage+ (char)codeValue;
					i++;
					if(i>= 32){
						i = i%32;
					}
				}catch (Exception e){
					System.out.println("");
				}
			}
		}
		catch(FileNotFoundException e){
			System.out.println("File does not exist");
		}
		//System.out.println("Message Read In: " + messageIn);
		
		
		try(PrintWriter pout = new PrintWriter(new FileOutputStream(fileOut, false))){
			System.out.println("=================================================");
			System.out.println("Decrypted message: " + decryptedMessage);
			System.out.println("=================================================");
			pout.println(decryptedMessage);
			System.out.println("Message decrypted.");
			pout.close();
		}
		catch (FileNotFoundException e){
			System.out.println("File does not exist");
		}
	}
}