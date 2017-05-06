package coolc.compiler.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Environment implements Map<String, String> {
	private Stack<HashMap<String, String>> layers;
	
	public Environment() {
		layers = new Stack<HashMap<String, String>>();
	}

	@Override
	public int size() {		
		return layers.peek().size();
	}

	@Override
	public boolean isEmpty() {
		return layers.size() == 1 && layers.peek().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		assert key instanceof String;
		
		for(int i = layers.size()-1; i>=0; i--) {
			if (layers.get(i).containsKey(key)) return true;
		}
		
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public String get(Object key) {
		assert key instanceof String;
		
		for(int i = layers.size()-1; i>=0; i--) {
			if (layers.get(i).containsKey(key)) 
				return layers.get(i).get(key);
		}
		
		return null;
	}

	@Override
	public String put(String key, String value) {
		// TODO: Validate redefinitions!
		return layers.peek().put(key, value);		
	}

	@Override
	public String remove(Object key) {
		return layers.peek().remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		// TODO Auto-generated method stub
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void newLevel() {
		layers.push(new HashMap<String, String>());
	}
	
	public void forgetLevel() {
		layers.pop();
	}

}
