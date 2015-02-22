package semantic;

public class FreeBaseTopic {
	
	private String id;
	private String type;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private String guid;
	private String[] mid;
	private String description;
	
	public FreeBaseTopic(String id, String type) {
		this.id = id;
		this.type = type;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String[] getMid() {
		return mid;
	}
	public void setMid(String[] mid) {
		this.mid = mid;
	}
	
	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		
		sb.append("FreeBaseTopic: id=").append(id).append(", guid=").append(guid);
		
		if (mid != null) {
			sb.append(", mid=[");
			for (String elem : mid) {
				sb.append(elem);
				sb.append(",");
			}
			sb.append("]");
		}
		
		sb.append(", desc=").append(description);
		
		return sb.toString();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
