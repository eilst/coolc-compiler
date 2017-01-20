package coolc.compiler.util;

import java.io.PrintStream;


public class Util {
    // sm: fixed an off-by-one error here; code assumed there were 80 spaces, but
    // in fact only 79 spaces were there; I've made it 80 now
    //                                         1         2         3         4         5         6         7
    //                               01234567890123456789012345678901234567890123456789012345678901234567890123456789
    private static String padding = "                                                                                "; // 80 spaces for padding
	
	public static String escapeString(String s) {
		StringBuffer b = new StringBuffer();
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\\':
				b.append("\\\\");
				break;
			case '\"':
				b.append("\\\"");
				break;
			case '\n':
				b.append("\\n");
				break;
			case '\t':
				b.append("\\t");
				break;
			case '\b':
				b.append("\\b");
				break;
			case '\f':
				b.append("\\f");
				break;
			default:
				if (c >= 0x20 && c <= 0x7f) {
					b.append(c);
				} else {
					String octal = Integer.toOctalString(c);
					b.append('\\');
					switch (octal.length()) {
					case 1:
						b.append('0');
					case 2:
						b.append('0');
					default:
						b.append(octal);
					}
				}
			}			
		}
		return b.toString();
	}
	
	public static void fatalError(String msg) {
		(new Throwable(msg)).printStackTrace();
		System.exit(1);
	}

    /** Returns the specified amount of space padding 
    *
    * @param n the amount of padding
    * */
   public static String pad(int n) {
       if (n > 80) return padding;
       if (n < 0) return "";
       return padding.substring(0, n);
   }
   
   /** Prints an appropritely escaped string
    * 
    * @param str the output stream
    * @param s the string to print
    * */
   public static void printEscapedString(PrintStream str, String s) {
       for (int i = 0; i < s.length(); i++) {
           char c = s.charAt(i);
           switch (c) {
           case '\\': str.print("\\\\"); break;
           case '\"': str.print("\\\""); break;
           case '\n': str.print("\\n"); break;
           case '\t': str.print("\\t"); break;
           case '\b': str.print("\\b"); break;
           case '\f': str.print("\\f"); break;
           default:
               if (c >= 0x20 && c <= 0x7f) {
                   str.print(c);
               } else {
                   String octal = Integer.toOctalString(c);
                   str.print('\\');
                   switch (octal.length()) {
                   case 1:
                       str.print('0');
                   case 2:
                       str.print('0');
                   default:
                       str.print(octal);
                   }
               }
           }
       }
   }


}
