package boc.message.server;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import boc.message.common.Request;

import com.google.common.collect.Maps;

public class RequestPool {

	private AtomicInteger nextGid = new AtomicInteger();

	private ConcurrentMap<Integer, ConcurrentMap<Long, Request>> requests = Maps.newConcurrentMap();

	public int newGroup() {
		int gid = nextGid.getAndIncrement();
		ConcurrentMap<Long, Request> g = Maps.newConcurrentMap();
		requests.put(gid, g);
		return gid;
	}

	public void delGroup(Integer gid) {
		requests.remove(gid);
	}

	public void addRequest(Integer gid, Request request) {
		ConcurrentMap<Long, Request> map = requests.get(gid);
		if (map != null) {
			map.put(request.getId(), request);
		}
	}

	public void delRequest(Integer gid, Long rid) {
		ConcurrentMap<Long, Request> map = requests.get(gid);
		if (map != null) {
			map.remove(rid);
		}
	}

	public int count() {
		int count = 0;
		for (ConcurrentMap<Long, Request> gmap : requests.values()) {
			count += gmap.size();
		}
		return count;
	}
}
