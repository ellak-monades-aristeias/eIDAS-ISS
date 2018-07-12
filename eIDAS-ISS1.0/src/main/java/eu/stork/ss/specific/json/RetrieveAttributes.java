package eu.stork.ss.specific.json;

import java.util.HashMap;

public class RetrieveAttributes {
	private String status;
	private HashMap<String, Attribute> list;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public HashMap<String, Attribute> getList() {
		return list;
	}

	public void setList(HashMap<String, Attribute> list) {
		this.list = list;
	}
}
