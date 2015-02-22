package semantic;

public class SemanticService {

	private SemanticDao dao;
	
	public SemanticService (SemanticDao dao) {
		this.dao = dao;
	}

	public DbpediaResource toDbpediaResource(FreeBaseTopic freeBaseTopic) {
		
		DbpediaResource dbpediaResource = null;
		
		String resource = dao.freebaseToDbpedia(FreeBaseClient.RDF_URL_TEMPLATE.replace("{x}", freeBaseTopic.getMid()[0]));
		
		System.out.println("corresponding dbpedia resource -> " + resource);
		
		if (resource!=null) {
			dbpediaResource = new DbpediaResource(resource);
			enhanceDbpediaResource(dbpediaResource);
		}
		
		return dbpediaResource;
	}
	
	private void enhanceDbpediaResource(DbpediaResource dbpediaResource) {
		
		String resource = dbpediaResource.getResource();
		
		dbpediaResource.setRedirects(dao.findRedirects(resource));
		dbpediaResource.setCategories(DbPediaClient.categories(resource));
		
		dbpediaResource.setHasDisambiguation(dao.haveDisambiguation(resource));
		
	}
	
	/*
	public Bag<String> _findRedirects(String name) {
		return dao.findRedirects(SemanticDaoImpl.DBPEDIA_RESOURCE_PRE+WordUtils.capitalize(name).replace(' ', '_')+SemanticDaoImpl.DBPEDIA_RESOURCE_POST);
	}
	 */
}
