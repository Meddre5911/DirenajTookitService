package semantic;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.TreeBag;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class SemanticDaoImpl implements SemanticDao {
	
	public final static String DBPEDIA_RESOURCE_PRE = "<http://dbpedia.org/resource/";
	public final static String DBPEDIA_RESOURCE_POST = ">";
	public final static String DBPEDIA_RESOURCE_DISAMBIGUATION_POST = "_%28disambiguation%29>";
	
	private JdbcTemplate jdbcTemplate;
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public Bag<String> findRedirects(String dbpediaResource) {
		
		List<String> list = jdbcTemplate.queryForList("SELECT replace(replace(replace(lower(concat(v_from,v_to)), '_',' ' ), '%28','('), '%29',')' ) FROM redirects_dbpedia WHERE v_from = ? OR v_to = ? ", 
                                                                                                    new Object[]{dbpediaResource,dbpediaResource}, String.class);

		return extractNames(list);
		
	}
	

	private Bag<String> extractNames(List<String> list) {
		
		Bag<String> names = new TreeBag<String>();
		
		for (String string : list) {
			names.addAll(Arrays.asList(StringUtils.substringsBetween(string, DBPEDIA_RESOURCE_PRE, DBPEDIA_RESOURCE_POST)));
		}
		
		return names;
	}
	
	/**
	 * 
	 */
	public String freebaseToDbpedia(String mid) {
		
		try {
			return jdbcTemplate.queryForObject("SELECT dbpedia FROM dbpedia_freebase WHERE freebase = ?", new Object[]{mid}, String.class);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	/**
	 * 
	 * @param dbpediaResource
	 * @return
	 */
	public boolean haveDisambiguation(String dbpediaResource) {
		return jdbcTemplate.queryForInt("SELECT count(*) FROM disambiguations_dbpedia where actual = ?", new Object[]{dbpediaResource}) > 0;
	}
	
}
