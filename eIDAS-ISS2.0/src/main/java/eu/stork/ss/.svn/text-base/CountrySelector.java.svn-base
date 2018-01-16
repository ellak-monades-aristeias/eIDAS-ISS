package eu.stork.ss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.Action;

public class CountrySelector extends AbstractAction {
	/**
	 * Unique identifier.
	 */
	private static final long serialVersionUID = -7878347835293457787L;

	//The logger
	static final Logger logger = LoggerFactory.getLogger(CountrySelector.class.getName());

	//Configuration properties
	private Properties configs; 

	//List of known countries
	private ArrayList<Country> countries;

	/**
	 * Check that the session variables are OK and display the list of known countries
	 * for the user to select. The selected country must be the users home CPEPS.
	 */
	public String execute() {
		HttpSession session;
		session = this.getSession();

		//Check that the session contains valid information
		synchronized(session) {
			if ( session==null || session.getAttribute(Constants.SP_TOKEN)==null
					|| session.getAttribute(Constants.SP_PAL)==null ) {
				String message = "Session is empty or contains invalid data!";

				logger.error(message);
				throw new ApplicationSpecificServiceException("Session error", message);
			}
		}

		//Load the configuration (list of countries)
		try {
			configs = SPUtil.loadConfigs(Constants.SP_PROPERTIES);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new ApplicationSpecificServiceException("Could not load configuration file", e.getMessage());
		}

		countries = new ArrayList<Country> ();
		int numCountries = Integer.parseInt(configs.getProperty(Constants.COUNTRY_NUMBER));
		for(int i=1; i<=numCountries; i++){
			Country country = new Country(i,configs.getProperty("country" + Integer.toString(i) + ".name"), configs.getProperty("country" + Integer.toString(i) + ".url"), configs.getProperty("country" + Integer.toString(i) + ".countrySelector"));
			countries.add(country);	
		}

		return Action.SUCCESS;
	}

	/**
	 * Get the list of known countries participating in STORK
	 * 
	 * @return The list of known CPEPS
	 */
	public ArrayList<Country> getCountries() {
		return countries;
	}

	/**
	 * Set the list of known countries participating in STORK
	 * 
	 * @param countries The countries list
	 */
	public void setCountries(ArrayList<Country> countries) {
		this.countries = countries;
	}
}