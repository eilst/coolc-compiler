package coolc.compiler;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PushbackReader;
import java.lang.reflect.Method;

import coolc.compiler.autogen.lexer.Lexer;
import coolc.compiler.autogen.lexer.LexerException;
import coolc.compiler.autogen.node.EOF;
import coolc.compiler.autogen.node.TCommentEnd;
import coolc.compiler.autogen.node.TCommentIn;
import coolc.compiler.autogen.node.TCommentStart;
import coolc.compiler.autogen.node.TErrorStarLpar;
import coolc.compiler.autogen.node.TLcommentStart;
import coolc.compiler.autogen.node.TLcommentEnd;
import coolc.compiler.autogen.node.Token;
import coolc.compiler.autogen.parser.Parser;
import coolc.compiler.util.Messages;
import coolc.compiler.util.*;

public class CoolcLexer extends Lexer {
	private Parser parser;
	private Method index;
	private Token lastToken;
	private PrintStream out;
	int comments = 0;

	public CoolcLexer(PushbackReader in, PrintStream out) {
		super(in);
		this.out = out;
	}
	
	public Token getLastToken() {
		return lastToken;
	}
	
	boolean ignore(Token t) {
		try {
			if ((int) ((Integer) index.invoke(parser, t)) == -1) {
				return true;
			}
		} catch (Exception e) {
			/* Never actually reached */
		}
		
		return false;
	}
	
	@Override
	public Token next() throws LexerException, IOException {
		return super.next();
	}
	
	protected void lexx(){
		//comments
		if (token instanceof TCommentStart) {
			comments++;
		}

		else if (token instanceof TCommentIn) {
			comments++;
		} 

		else if (token instanceof TCommentEnd) {
			comments--;
			if (comments == 0) {
				state = State.INITIAL;
			} else {
				state = State.COMMENT;
			}
		}
		//comments errors
		commenterrors();//
	}
	protected void commenterrors(){
		 if (token instanceof TErrorStarLpar) {
			out.format(Messages.getString("Coolc.lexer.unmatchedCloseComment"), token.getLine()); 

		 }
	}

	@Override
	protected void filter() throws LexerException, IOException {
			lexx();
			while (ignore(token)) {
			token = getToken();
			lexx();
			}
	}

	public void setParser(Parser p) {
		parser = p;
		/* Children, do not do reflection without supervision */

		try {
			for (Method m : parser.getClass().getDeclaredMethods()) {
				if ("index".equals(m.getName())) { //$NON-NLS-1$
					m.setAccessible(true);
					this.index = m;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public String unescape(String s) {
		StringBuffer b = new StringBuffer();
		
		for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);        

            if (c == '\\') {
                switch(s.charAt(i+1)) {
                    case 'b':
                        i++;
                        b.append("\b"); //$NON-NLS-1$
                        break;
                    case 't':
                        i++;
                        b.append("\t"); //$NON-NLS-1$
                        break;
                    case 'n':
                        i++;
                        b.append("\n"); //$NON-NLS-1$
                        break;
                    case 'f':
                        i++;
                        b.append("\f"); //$NON-NLS-1$
                        break;
                    case '\\':
                        i++;
                        b.append("\\"); //$NON-NLS-1$
                        break;
                }
            } else {
                b.append(c);
            }
        }

        return b.toString();		
	}

}