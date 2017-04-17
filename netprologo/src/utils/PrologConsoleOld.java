package utils;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Scanner;

import javax.swing.JFrame;

import jpl.Query;

public class PrologConsole extends Panel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	// Members used for presenting output
	private TextAreaWriter m_taw;
	//private PrintWriter error = new PrintWriter("error.log", "UTF-8");

	// Members used for getting input
	private TextField m_tf;

	// TextReader is in the jess package
	private TextReader m_in;

	private Query q;


	public PrologConsole()
	{
		
		// Set up the GUI elements
		TextArea ta = new TextArea(10, 40);
		m_tf = new TextField(50);
		ta.setEditable(false);
		Panel p = new Panel();
		p.setLayout(new BorderLayout());

		// arrange buttons   
		Button bClear = new Button("Clear Window");   

		// Set up I/O streams
		m_taw = new TextAreaWriter(ta);
		m_in = new TextReader(false);

		// Assemble the GUI
		
		setLayout(new BorderLayout());
		add("Center", ta);
		p.add("Center", m_tf);
		p.add("East", bClear);
		add("South", p);
		
		this.setVisible(true);


		
		PrintWriter pw = new PrintWriter(m_taw, true);
		Scanner scanner = new Scanner(m_in);
		while (scanner.hasNext())
			pw.print(scanner.next());
		scanner.close();


		
		/*r.addInputRouter("t", m_in, true);
		r.addOutputRouter("t", pw);
		r.addInputRouter("WSTDIN", m_in, true);
		r.addOutputRouter("WSTDOUT", pw);
		r.addOutputRouter("WSTDERR", error);*/
	}

	/**
	 * Move focus to the input area. Helps to call this whenever a button is clicked, etc.
	 */

	final public void setFocus() { m_tf.requestFocus(); }

	public static void main(String[] args)
	{
		System.out.println("Entering...");
		
		JFrame consoleframe = new JFrame("XXX");
		consoleframe.add(new PrologConsole());
		consoleframe.pack();
		consoleframe.setVisible(true);
		System.out.println("Exiting...");
	}

}



class TextAreaWriter extends Writer implements Serializable {
    private StringBuffer m_str = new StringBuffer(100);
    private TextArea m_ta;
    private int m_size = 0;
    private static final int MAXSIZE = 30000;

    public TextAreaWriter(TextArea textArea) {
        this.m_ta = textArea;
        this.m_size = this.m_ta.getText().length();
    }

    public synchronized void clear() {
        this.m_ta.setText("");
        this.m_size = 0;
    }

    public void close() {
    }

    public synchronized void flush() {
        int n = this.m_str.length();
        if (this.m_size > 30000) {
            this.m_ta.replaceRange("", 0, n);
            this.m_size -= n;
        }
        this.m_ta.append(this.m_str.toString());
        this.m_size += n;
        this.m_str.setLength(0);
    }

    public synchronized void write(char[] arrc, int n, int n2) {
        this.m_str.append(arrc, n, n2);
    }
}

class TextReader extends Reader implements Serializable {
    private StringBuffer m_buf = new StringBuffer(256);
    private int m_ptr = 0;
    private boolean m_dontWait = false;
    private boolean m_open = true;

    public TextReader(boolean bl) {
        this.m_dontWait = bl;
    }

    public synchronized int read() throws IOException {
        if (!this.m_open) {
            throw new IOException("Closed");
        }
        while (this.m_ptr >= this.m_buf.length()) {
            if (this.m_dontWait) {
                return -1;
            }
            if (!this.m_open) {
                return -1;
            }
            try {
                this.wait(100);
                continue;
            }
            catch (InterruptedException var1_1) {
                if (this.m_open) continue;
                return -1;
            }
        }
        char c = this.m_buf.charAt(this.m_ptr++);
        if (this.m_ptr >= this.m_buf.length()) {
            this.clear();
        }
        return c;
    }

    public int read(char[] arrc) throws IOException {
        return this.read(arrc, 0, arrc.length);
    }

    public int read(char[] arrc, int n, int n2) throws IOException {
        for (int i = n; i < n + n2; ++i) {
            int n3 = this.read();
            if (n3 == -1) {
                return i > n ? i - n : -1;
            }
            arrc[i] = (char)n3;
        }
        return n2;
    }

    public void close() {
        this.m_open = false;
    }

    public int available() {
        return this.m_buf.length() - this.m_ptr;
    }

    public synchronized void appendText(String string) {
        this.m_buf.append(string);
        this.notifyAll();
    }

    public synchronized void clear() {
        this.m_buf.setLength(0);
        this.m_ptr = 0;
    }
}
