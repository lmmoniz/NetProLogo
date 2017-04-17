package primitives;

import main.NetPrologoExtension;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

//just opens a console to call prolog queries
public class Console extends DefaultReporter {

	// The first argument should be a String.

    public Syntax getSyntax() {
        return Syntax.reporterSyntax(new int[] {Syntax.StringType()}, Syntax.BooleanType());
    }
  
	
	public Object report(Argument[] arg0, Context arg1) throws ExtensionException{
        String call;
        try{
            call = arg0[0].getString();
        }catch( LogoException e ){
        	throw new ExtensionException(e.getMessage());
        }
        
    	return NetPrologoExtension.console(); 
	}
}
