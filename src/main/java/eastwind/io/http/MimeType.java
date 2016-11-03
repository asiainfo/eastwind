package eastwind.io.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

public class MimeType {

	private static Map<String, String> typeMapper = Maps.newHashMap();

	static {
		ClassPathResource resource = null;
		try {
			resource = new ClassPathResource("mimeType");
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
			for (String line = null; (line = br.readLine()) != null;) {
				if (StringUtils.isBlank(line)) {
					continue;
				}
				Iterator<String> it = Splitter.onPattern("[ \t]").trimResults().omitEmptyStrings().split(line)
						.iterator();
				String extension = null;
				String mimeType = null;
				if (it.hasNext()) {
					extension = it.next();
				}
				if (it.hasNext()) {
					mimeType = it.next();
				}
				if (extension.startsWith(".")) {
					extension = extension.substring(1);
				}
				if (mimeType != null) {
					typeMapper.put(extension, mimeType);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static String getType(String extension) {
		String type = typeMapper.get(extension);
		return type == null ? "application/octet-stream" : type;
	}

}
