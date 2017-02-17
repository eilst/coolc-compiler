package coolc.compiler;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PushbackReader;
import java.lang.reflect.Method;

import coolc.compiler.autogen.lexer.Lexer;
import coolc.compiler.autogen.lexer.LexerException;
import coolc.compiler.autogen.node.EOF;
import coolc.compiler.autogen.node.TCommentBegin;
import coolc.compiler.autogen.node.TCommentEnd;
import coolc.compiler.autogen.node.TCommentNest;
import coolc.compiler.autogen.node.TErrend;
import coolc.compiler.autogen.node.TErrorStarLpar;
import coolc.compiler.autogen.node.TStrBegin;
import coolc.compiler.autogen.node.TStrConst;
import coolc.compiler.autogen.node.TStrEnd;
import coolc.compiler.autogen.node.TStrErrOutDq;
import coolc.compiler.autogen.node.TStrErrOutNl;
import coolc.compiler.autogen.node.TStrErrorEol;
import coolc.compiler.autogen.node.TStrErrorEscapedNull;
import coolc.compiler.autogen.node.TStrErrorNull;
import coolc.compiler.autogen.node.Token;
import coolc.compiler.autogen.parser.Parser;
import coolc.compiler.util.Messages;
import coolc.compiler.util.Util;

public class CoolcLexer extends Lexer {
	private Parser parser;
	private Method index;
	private PrintStream out;
	private Token lastToken;

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

	int comments = 0;
	boolean inStr = false, jumpOne = false, errorNulInStr = false;
	TStrConst tempStr = null;

	protected void checks() {
		// Comentarios anidados.
		if (token instanceof TCommentBegin) {
			comments++;
		}

		else if (token instanceof TCommentNest) {
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

			
		// Comienzo de string
		else if (token instanceof TStrBegin) {
			inStr = true;
		}
			
		// Composición final del string
		else if (token instanceof TStrEnd) {
			if (inStr) {
				if (tempStr == null) {
					token = new TStrConst("", token.getLine(), token.getPos()); //$NON-NLS-1$
				} else {
					tempStr.setText(unescape(tempStr.getText()));
					token = tempStr;
				}
				if (token.getText().length() > 1024) {
					out.format(Messages.getString("Coolc.lexer.longString"), token.getLine()); //$NON-NLS-1$
					jumpOne = true;
				}
				if (errorNulInStr) {
					out.format(Messages.getString("Coolc.lexer.nullInString"), //$NON-NLS-1$
							token.getLine());
					errorNulInStr = false;
					jumpOne = true;					
				}
				tempStr = null;
				inStr = false;
			}
		}

		// Acumulación en strings intermedios del string
		else if (token instanceof TStrConst) {
			if (tempStr == null) {
				tempStr = (TStrConst) token;
			} else {
				tempStr.setText(tempStr.getText() + token.getText());
			}
		}

		// Errores en medio del string
		else if (token instanceof TStrErrorEol) {
			out.format(Messages.getString("Coolc.lexer.unterminatedString"), token.getLine()); //$NON-NLS-1$
			tempStr = null;
			inStr = false;
			jumpOne = true;			
		}

		else if (token instanceof TStrErrorNull) {
			out.format(Messages.getString("Coolc.lexer.nullInString"), token.getLine()); //$NON-NLS-1$
		}
		
		else if (token instanceof TStrErrorEscapedNull) {
			out.format(Messages.getString("Coolc.lexer.escapedNullString"), token.getLine()); //$NON-NLS-1$
		} 
		else if (token instanceof TStrErrOutNl) {
			tempStr = null;
			inStr = false;
			jumpOne = true;
		}
		else if (token instanceof TStrErrOutDq) {
			tempStr = null;
			inStr = false;
			jumpOne = true;
		}


		// Errores por fin de archivo
		else if (token instanceof EOF) {
			if (inStr) {
				out.format(Messages.getString("Coolc.lexer.EOFInString"), //$NON-NLS-1$
						token.getLine());
				inStr = false;
			}
			if (comments > 0) {
				out.format(Messages.getString("Coolc.lexer.EOFInComment"), token.getLine()); //$NON-NLS-1$
				comments = 0;
			}
		}
		
		// Error por fin de comentario encontrado		
		else if (token instanceof TErrorStarLpar) {
			out.format(Messages.getString("Coolc.lexer.unmatchedCloseComment"), token.getLine()); //$NON-NLS-1$
			jumpOne = true;
		} 
		
		// Error sobre cualquier token extraño al final
		else if (token instanceof TErrend) {
			out.format(Messages.getString("Coolc.lexer.generalError"), token.getLine(), Util.escapeString(token.getText())); //$NON-NLS-1$
			jumpOne = true;
		}
	}
	
	@Override
	public Token next() throws LexerException, IOException {
		lastToken = token;
		return super.next();
	}

	@Override
	protected void filter() throws LexerException, IOException {		
		checks();
		while (ignore(token) || inStr || jumpOne) {
			token = getToken();
			jumpOne = false;
			checks();
		}
	}

	public void setParser(Parser p) {
		parser = p;
		/* Niños, no hagan esto sin la supervisión de un adulto */

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
