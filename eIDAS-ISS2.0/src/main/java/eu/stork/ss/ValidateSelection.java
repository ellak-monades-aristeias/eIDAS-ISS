package eu.stork.ss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedSet;
import com.opensymphony.xwork2.Action;

import eu.stork.peps.auth.commons.PEPSUtil;
import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.STORKAuthnRequest;
import eu.stork.peps.auth.engine.STORKSAMLEngine;
import eu.stork.peps.exceptions.STORKSAMLEngineException;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.stork.peps.auth.commons.PersonalAttribute;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.sp.SpAuthenticationRequestData;
import eu.eidas.sp.SpEidasSamlTools;
import eu.eidas.sp.SpProtocolEngineFactory;
import eu.eidas.sp.SpProtocolEngineI;

public class ValidateSelection extends AbstractAction {
	/**
	 * Unique identifier.
	 */
	private static final long serialVersionUID = -7878347835293457787L;

	//The logger
	static final Logger logger = LoggerFactory.getLogger(ValidateSelection.class.getName());

	//Configuration properties
	private Properties configs; 

	//List of known countries
	private ArrayList<Country> countries;

	//Indicates that an error was found (since the validate returns false)
	private int errorFound;

	//The URL to send the SAML request
	private String spepsUrl;

	//The SAML request
	private String samlToken;
	
	private String citizenCountry;
	
	/**
	 * Check that the session variables are OK and display the list of known countries
	 * for the user to select. The selected country must be the users home CPEPS.
	 */
	public String execute() {
		HttpSession session;
		Country selectedCountry = null;
		session = this.getSession();
		String selectedId = null;
		selectedId = (String)session.getAttribute("cc");
		session.removeAttribute("cc");
		System.out.println("selectedId "+selectedId);
		if (selectedId == null) selectedId = this.getServletRequest().getParameter(Constants.SP_PARAM_COUNTRYID);
		
		//Check that the session contains valid information
		synchronized(session) {
			if ( session==null || session.getAttribute(Constants.SP_TOKEN)==null
					|| session.getAttribute(Constants.SP_PAL)==null ) {
				String message = "Session is empty or contains invalid data!";
				Monitoring.monitoringLog( "<span class='error'>Step 3: Error!</span>");
				logger.error(message);
				throw new ApplicationSpecificServiceException("Session error", message);
			}
		}

		//Load the configuration (list of countries)
		try {
			configs = SPUtil.loadConfigs(Constants.SP_PROPERTIES);
		} catch (IOException e) {
			Monitoring.monitoringLog( "<span class='error'>Step 3: Error!</span>");
			logger.error(e.getMessage());
			throw new ApplicationSpecificServiceException("Could not load configuration file", e.getMessage());
		}

		countries = new ArrayList<Country> ();
		int numCountries = Integer.parseInt(configs.getProperty(Constants.COUNTRY_NUMBER));
		for(int i=1; i<=numCountries; i++){
			Country country = new Country(i,configs.getProperty("country" + Integer.toString(i) + ".name"), configs.getProperty("country" + Integer.toString(i) + ".url"), configs.getProperty("country" + Integer.toString(i) + ".countrySelector"));
			countries.add(country);

			if ( selectedId!=null && selectedId.equals(country.getName()) )
				selectedCountry = country;
			
			System.out.println("selectedId "+selectedId+" "+country.getName());
		}

		//Validate user selection and prepare the SAML document
		if ( selectedCountry == null ) 
		{
			logger.debug("No country selected! Present error...");
			setErrorFound(1);
			return Action.INPUT;
		}
		this.citizenCountry = selectedCountry.getName();
		Monitoring.monitoringLog( "Selected Country: "+ selectedCountry.getName());
		
		String samlType = (String)session.getAttribute("saml");
		if (samlType != null && samlType.equals("eIDAS"))
		{
			int qaaLvl = 2;
			if (session.getAttribute("qaa") == null) qaaLvl = Integer.parseInt(configs.getProperty(Constants.SP_QAALEVEL));
			else qaaLvl = Integer.parseInt((String)session.getAttribute("qaa"));
		    String serviceProviderCountry = configs.getProperty(Constants.SP_COUNTRY);
		    ArrayList<String> pals = new ArrayList<String>();
		    IPersonalAttributeList pal = (IPersonalAttributeList)this.getSession().getAttribute(Constants.SP_PAL);
		    for (PersonalAttribute pa: pal)
		    	pals.add(pa.getName());
		    
		    
			SpAuthenticationRequestData data = SpEidasSamlTools.generateEIDASRequest(pals, citizenCountry, serviceProviderCountry, qaaLvl);
			samlToken = data.getSaml();
			System.out.println("Request ID: "+data.getID());
			session.setAttribute(Constants.SAML_IN_RESPONSE_TO_SP, data.getID());
			session.setAttribute(Constants.ISSUER_SP, configs.getProperty("sp.metadata.url"));
			this.spepsUrl = SpEidasSamlTools.getNodeUrl();
			

			Monitoring.monitoringLog( "<span class='success'>Step 3: Success!</span>");
			return Action.SUCCESS;
		}
		else
		{
			byte[] token = null;		
			this.spepsUrl = configs.getProperty(Constants.SPEPS_URL);
			STORKAuthnRequest authnRequest = new STORKAuthnRequest();

			logger.debug("Selection OK, starting SAML generation. Country: Id:[" + selectedCountry.getId()
					+ "] Name:[" + selectedCountry.getName() + "] URL:[" + selectedCountry.getUrl() + "]");

			authnRequest.setDestination(this.spepsUrl);
			authnRequest.setSpCountry(configs.getProperty(Constants.SP_COUNTRY));

			//V-IDP parameters
			authnRequest.setCitizenCountryCode(citizenCountry);

			authnRequest.setProviderName(configs.getProperty(Constants.PROVIDER_NAME));	
			if (session.getAttribute("qaa") == null) authnRequest.setQaa(Integer.parseInt(configs.getProperty(Constants.SP_QAALEVEL)));
			else authnRequest.setQaa(Integer.parseInt((String)session.getAttribute("qaa")));
			authnRequest.setPersonalAttributeList((IPersonalAttributeList)this.getSession().getAttribute(Constants.SP_PAL));
			authnRequest.setAssertionConsumerServiceURL(configs.getProperty(Constants.SP_RETURN));

			//new parameters
			authnRequest.setSpSector(configs.getProperty(Constants.SP_SECTOR));
			authnRequest.setSpApplication(configs.getProperty(Constants.SP_APLICATION));

			//V-IDP parameters
			authnRequest.setSPID(configs.getProperty(Constants.PROVIDER_NAME));

			try{
				STORKSAMLEngine engine = STORKSAMLEngine.getInstance(Constants.SP_CONF);
				authnRequest = engine.generateSTORKAuthnRequest(authnRequest);

				//Store in session in order to validate them in the response
				session.setAttribute(Constants.SAML_IN_RESPONSE_TO_SP, authnRequest.getSamlId());
				session.setAttribute(Constants.ISSUER_SP, authnRequest.getIssuer());
				//--
			}catch(STORKSAMLEngineException e){
				Monitoring.monitoringLog( "<span class='error'>Step 3: Error!</span>");
				logger.error(e.getMessage());
				throw new ApplicationSpecificServiceException("Could not generate token for Saml Request", e.getErrorMessage());
			}
					
			token = authnRequest.getTokenSaml();			
			this.samlToken = PEPSUtil.encodeSAMLToken(token);
			
			Monitoring.monitoringLog( "<span class='success'>Step 3: Success!</span>");
			return Action.SUCCESS;
		}
		
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

	/**
	 * Returns the error number in order to display the appropriate message
	 * 
	 * @return The error number
	 */
	public int getErrorFound() {
		return errorFound;
	}

	/**
	 * Set the error number that was found
	 * 
	 * @param errorFound The error number
	 */
	public void setErrorFound(int errorFound) {
		this.errorFound = errorFound;
	}

	/**
	 * Setter for samlToken.
	 * 
	 * @param samlToken The samlToken to set.
	 */
	public void setSamlToken(final String samlToken) {
		this.samlToken = samlToken;
	}

	/**
	 * Getter for samlToken.
	 * 
	 * @return The samlToken value.
	 */
	public String getSamlToken() {
		return samlToken;
	}

	/**
	 * Setter for spepsUrl.
	 * 
	 * @param spepsUrl The spepsUrl to set.
	 */
	public void setSpepsUrl(final String spepsUrl) {
		this.spepsUrl = spepsUrl;
	}

	/**
	 * Getter for spepsUrl.
	 * 
	 * @return The spepsUrl value.
	 */
	public String getSpepsUrl() {
		return spepsUrl;
	}
	
	public void setCitizenCountry(final String citizenCountry) {
		this.citizenCountry = citizenCountry;
	}

	/**
	 * Getter for samlToken.
	 * 
	 * @return The samlToken value.
	 */
	public String getCitizenCountry() {
		return citizenCountry;
	}
}