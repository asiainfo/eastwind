package eastwind.io.http;

import java.util.List;

import com.google.common.base.Splitter;

public class PathDecoder {

	private List<String> paths;
	
	public PathDecoder(String path) {
		this.paths = Splitter.on("/").omitEmptyStrings().splitToList(path);
	}
	
	public String getFirst() {
		return paths.get(0);
	}
	
	public String getSecond() {
		return paths.size() < 2 ? null : paths.get(1);
	}
	
	public String get(int index) {
		return paths.size() <= index ? null : paths.get(index);
	}
}
