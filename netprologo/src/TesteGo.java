import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Pattern;

import jpl.Term;


public class TesteGo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<String> vars = predicateSplit("pred(X,Y,2,a,Z).");
		int i = 0;
		while(i< vars.size())
		{
			if (vars.get(i).charAt(0) > 'Z' || vars.get(i).charAt(0) < 'A')
				vars.remove(i);
			else
				i++;
		}
 		System.out.println(vars.toString());
		}
	
    static ArrayList<String> predicateSplit(String p){
    	String[] token = {"\\(","\\.","\\,","\\)"};
		ArrayList<String> res =new ArrayList<String>();
		res.add(p);
		for(int i = 0; i< token.length;i++) {
			ArrayList<String> aux = new ArrayList<String>();
			for(int j = 0; j<res.size();j++)
				aux.addAll(Arrays.asList(res.get(j).split(token[i])));
			res = aux;
		}
		return res;
    }

	}

