package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import jpl.Query;
import jpl.Term;

import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.PrimitiveManager;

import primitives.BuildPrologCall;
import primitives.Close;
import primitives.DereferenceVarInStore;
import primitives.DereferenceVarJPL;
import primitives.NextSolutionInStore;
import primitives.RunNextJPL;
import primitives.RunQueryJPL;
import primitives.RunQueryNsolutions;
import primitives.Console;
import utils.PathManagement;
import utils.PrologConsole;
import utils.SolStore;

public class NetPrologoExtension extends DefaultClassManager {

	// Active Prolog query.
	private static Query jplQuery=null;
	// Denotes if there is an active console.
	private static Boolean console = false;
	private static JFrame consoleframe;
	// Denotes if there is an active query.
	private static boolean finished = true;
	// Stores current solution
	private static Hashtable jplSolution;
	// Id generator for solutions Stores.
	private static int idCounter;
	// Maps active solitions Stores with their ids.
	private static Hashtable<Integer,SolStore> solutionsStore;
	//Keep the variables of a query
	private static ArrayList<String> vars;

	public java.util.List<String> additionalJars() {
		java.util.List<String> list = new java.util.ArrayList<String>();
		list.add("jpl.jar");
		return list;
	}

	@Override
	public void load(PrimitiveManager arg0) throws ExtensionException {
		arg0.addPrimitive("build-prolog-call", new BuildPrologCall());
		arg0.addPrimitive("run-query", new RunQueryJPL());
		arg0.addPrimitive("run-next", new RunNextJPL());
		arg0.addPrimitive("dereference-stored-var", new DereferenceVarInStore());
		arg0.addPrimitive("run-for-n-solutions", new RunQueryNsolutions());
		arg0.addPrimitive("next-stored-solution", new NextSolutionInStore());
		arg0.addPrimitive("dereference-var", new DereferenceVarJPL());
		arg0.addPrimitive("close-query", new Close());
		arg0.addPrimitive("console", new Console());
	}

	public void runOnce(org.nlogo.api.ExtensionManager em) throws ExtensionException {
		try {
			initializeExtension();
		} catch (Exception e) {
			throw new ExtensionException(e);
		} 

	}

	// Initialize solutions Store and updates system path.
	private static void initializeExtension() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		idCounter=0;
		solutionsStore=new Hashtable<Integer,SolStore>();

		if(PathManagement.getOs().indexOf("win") < 0){
			PathManagement.updateSystemPath();
		}
	}

	// Close current query.
	public static void release(){
		if(jplQuery!=null){
			jplQuery.rewind();
			jplSolution = null;
			finished=true;
		}
	}

	// Opens a new Prolog query.
	public static boolean runQueryJPL(String q){
		release();
		jplQuery=new Query(q);
		if(jplQuery.hasSolution()){ // If there are not more solutions to be read ...
			finished=false;
			return true;
		}else{
			finished=true;
			return false;
		}
	}

	// Opens a new Prolog query.
	public static StringBuffer runQueryJPLSolutions(String q){
		release();
		StringBuffer ans = new StringBuffer("");
		jplQuery=new Query(q);
		//Get all VARS
		ArrayList<String> vars = predicateSplit(q);
		int i = 0;
		while(i< vars.size())
		{
			if ((vars.get(i).charAt(0) > 'Z' || vars.get(i).charAt(0) < 'A') && (vars.get(i).charAt(0) != '_'))
				vars.remove(i);
			else
				i++;
		}
		if(jplQuery.hasSolution()){ // If there are not more solutions to be read ...
			finished=false;
			while (jplQuery.hasMoreElements()){
				for(String var : vars)
				{
					Term bound_to_ = (Term) ((Hashtable) jplQuery.nextElement()).get(var);
					ans = bound_to_==null?ans:ans.append(var+"="+bound_to_+",");
				}
				ans = new StringBuffer(ans.substring(0, ans.length()-1).concat(";\n"));
			}
			return new StringBuffer(ans.length()==0?"true":ans);
		}else{
			finished=true;
			return new StringBuffer("false");
		}
	}
	
	public static StringBuffer runQueryJPLConsole(String q){
		release();
		jplQuery = new Query(q);
		//Get all VARS
		vars = predicateSplit(q);
		finished = false;
		int i = 0;
		while(i< vars.size())
		{
			if ((vars.get(i).charAt(0) > 'Z' || vars.get(i).charAt(0) < 'A') && (vars.get(i).charAt(0) != '_'))
				vars.remove(i);
			else
				i++;
		}
		return runNextJPLConsole();
		}

	
	public static StringBuffer runNextJPLConsole(){
		StringBuffer ans = new StringBuffer("");
		if(!finished && jplQuery.hasMoreSolutions()){ // If there are not more solutions to be read ...
			finished=false;
			jplSolution = jplQuery.nextSolution();
				for(String var : vars)
				{
					Term bound_to_ = (Term) (jplSolution.get(var));
					ans = bound_to_==null?ans:ans.append(var+"="+bound_to_+",");
				}
				ans = new StringBuffer(ans.substring(0, ans.length()-1).concat(";\n"));
			return new StringBuffer(ans.length()==0?"true":ans);
		}else{
			finished=true;
			return new StringBuffer("false");
		}
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

	// Run a new query and store its first N solutions.
	public static int runQueryNsolutions(int n,String q){
		release();
		jplQuery=new Query(q);
		int count=0;
		ArrayList<Hashtable> sols=new ArrayList<Hashtable>();
		while(jplQuery.hasMoreSolutions() && count < n){
			sols.add(jplQuery.nextSolution());
			count++;
		}
		int id=idCounter++;
		solutionsStore.put(id, new SolStore(sols));
		return id;
	}

	// Load the next solution of the currently active Prolog query.
	public static boolean runNextJPL(){
		if (!jplQuery.hasMoreSolutions() || finished){
			finished = true;
			return false;
		}else{
			jplSolution=jplQuery.nextSolution();
			return true;
		}
	}

	// Dereference a specific variable from the last loaded solution of the active query.
	public static Object dereferenceVarJPL(String varName) throws ExtensionException {
		return jplSolution.get(varName);
	}

	// Load the next solution for a certain solutions store
	public static Object nextSolutionInStore(int storeId) {
		boolean ret=solutionsStore.get(storeId).nextSolution();
		if(!ret)
			solutionsStore.remove(storeId);
		return ret;
	}

	// Dereference a specific variable from the last solution loaded of a certain solution store.
	public static Object dereferenceVarInStore(int storeId, String varName) {
		return solutionsStore.get(storeId).dereferenceVar(varName);
	}

	// Open a active console.
	public static Object console() throws ExtensionException {
		if (!console) {
			console = true;
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					consoleframe = new PrologConsole();
					consoleframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					consoleframe.setLocationRelativeTo(null);
					consoleframe.pack();
					consoleframe.setVisible(true);
				}
			});
		}
		else {
			consoleframe.setVisible(false);
			console = false;
			consoleframe.removeAll();
		}
		return null;
	}
}
