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
import coolc.compiler.autogen.node.TErrend;
import coolc.compiler.autogen.node.TErrorStarLpar;
import coolc.compiler.autogen.node.TStrConst;
import coolc.compiler.autogen.node.TStrEnd;
import coolc.compiler.autogen.node.TStrErrNlb;
import coolc.compiler.autogen.node.TStrErrQuotesb;
import coolc.compiler.autogen.node.TStrErrorEol;
import coolc.compiler.autogen.node.TStrErrorEscapedNull;
import coolc.compiler.autogen.node.TStrErrorNull;
import coolc.compiler.autogen.node.TStrStart;
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
	boolean strst = false;
	boolean pass = false; 
	boolean errorNulInStr = false;
	TStrConst buffer = null;

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
		//to handle nested comments
		if (token instanceof TCommentStart) {
			comments++;
		}
		else if (token instanceof TCommentIn) {
			comments++; //for nested comments
		} 
		else if (token instanceof TCommentEnd) {
			comments--; //closing nested comments until the  initial commentstart is matched
			if (comments == 0) {
				state = State.INITIAL;
			} else {
				state = State.COMMENT;
			}
		}
		
		//comment errors
		commenterrors();//
		
		//strings
		 if (token instanceof TStrStart) {
			strst = true;
		}
		else if (token instanceof TStrEnd) {
			if (strst) {
				if (buffer != null) {
					buffer.setText(unescape(buffer.getText()));
					token = buffer;
				} else {
					token = new TStrConst("", token.getLine(), token.getPos()); //as seen in class
					
				}
				if (errorNulInStr) {
					out.format(Messages.getString("Coolc.lexer.nullInString"), 
							token.getLine());
					errorNulInStr = false;
				}
				if (token.getText().length() > 1024) {
					out.format(Messages.getString("Coolc.lexer.longString"), token.getLine()); 
					pass = true; //pass s will have effect when reaching the while loop on ignore tokens
				}
				strst = false;
				buffer = null;
			}
		}
		 else if (token instanceof TStrConst) {
			if (buffer == null) {
				buffer = (TStrConst) token;
			} else {
				buffer.setText(buffer.getText() + token.getText());
			}
		}
		stringErrors();
	}
	
	protected void stringErrors(){
		if (token instanceof TStrErrorEol) {
			out.format(Messages.getString("Coolc.lexer.unterminatedString"), token.getLine()); 
			buffer = null;
			strst = false;
		}
	
		else if (token instanceof TStrErrorNull) {
			out.format(Messages.getString("Coolc.lexer.nullInString"), token.getLine()); 
		}
		
		else if (token instanceof TStrErrorEscapedNull) {
			out.format(Messages.getString("Coolc.lexer.escapedNullString"), token.getLine()); 
		} 
		else if (token instanceof TStrErrNlb) {
			buffer = null;
			strst = false;
		}
		else if (token instanceof TStrErrQuotesb) {
			buffer = null;
			strst = false;
		}
		else if (token instanceof EOF && strst ) {
				out.format(Messages.getString("Coolc.lexer.EOFInString"), 
						token.getLine());
				strst = false;
		}
		else if ( token instanceof EOF && comments > 0) {
			out.format(Messages.getString("Coolc.lexer.EOFInComment"), token.getLine()); 
			comments = 0;
		}
		else if (token instanceof TErrend) {
			if (token.getText().contains("\\") ){ //fas seen in class, \ is a special character 
												  //and needs to be represented as \\ to be recognized
				token.setText("\\\\");
			}
			if (token.getText().contains("\001") ){//for invisisible characters on tests
				token.setText("\\001");
			}
			if (token.getText().contains("\002") ){
				token.setText("\\002");
			}
			if (token.getText().contains("\003") ){
				token.setText("\\003");
			}
			if (token.getText().contains("\004") ){
				token.setText("\\004");
			}
			if (token.getText().contains("\000") ){ // for null character
				token.setText("\\000");
			}
			out.format(Messages.getString("Coolc.lexer.generalError"), token.getLine(), token.getText());	
		}
	}
	
	protected void commenterrors(){
		 if (token instanceof TErrorStarLpar) {
			out.format(Messages.getString("Coolc.lexer.unmatchedCloseComment"), token.getLine()); 
		 }
	}

	@Override
	protected void filter() throws LexerException, IOException {
			lexx();
			while (ignore(token) || strst || pass) {//enter ignoreloop if token belongs to ignore Tokens, text in string or if 
												
			token = getToken();
			pass=false;
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