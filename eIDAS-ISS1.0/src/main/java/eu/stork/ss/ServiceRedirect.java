package eu.stork.ss;

import java.io.IOException;
import java.util.Properties;

import org.bouncycastle.util.encoders.Base64;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.Action;

import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.PEPSUtil;
import eu.stork.peps.auth.commons.STORKAuthnResponse;
import eu.stork.peps.auth.engine.STORKSAMLEngine;
import eu.stork.peps.exceptions.SAMLEngineException;
import eu.stork.peps.exceptions.STORKSAMLEngineException;

import eu.stork.peps.auth.engine.SAMLEngine;

public class ServiceRedirect extends AbstractAction {
	/**
	 * Unique identifier.
	 */
	private static final long serialVersionUID = -5921651086873165403L;

	//The logger
	static final Logger logger = LoggerFactory.getLogger(ServiceRedirect.class.getName());

	//The PAL
	private IPersonalAttributeList pal;

	/**
	 * SAML token.
	 */
	private String SAMLResponse;

	/**
	 * Validate SAML token and extract the PersonalAttributeList.
	 */
	private String failUrl;
	
	public String execute() {
		ApplicationSpecificServiceException exception = null;
		
		Properties configs=null;
		String sp = this.getSession().getAttribute("sp").toString();
		try {
			configs = SPUtil.loadConfigs(Constants.SP_PROPERTIES);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		failUrl = configs.getProperty(sp+".sf.url");
		String token = (String)this.getSession().getAttribute(Constants.SP_TOKEN);
		failUrl+=token;
		
		try {
			byte[] decSamlToken = PEPSUtil.decodeSAMLToken(SAMLResponse);
			Monitoring.monitoringLog( " Response Token: "+new String(decSamlToken, "UTF-8"));
			//Get SAMLEngine instance
			STORKSAMLEngine engine = STORKSAMLEngine.getInstance(Constants.SP_CONF);

			try {
				//validate SAML Token
				STORKAuthnResponse authnResponse = engine.validateSTORKAuthnResponseWithQuery(decSamlToken, (String)getServletRequest().getRemoteHost());				

				if( authnResponse.isFail() ){
					exception = new ApplicationSpecificServiceException("Saml Response had failed!", authnResponse.getMessage());
				}
				else {
					if ( validateResponse(authnResponse) )
					{
						pal = authnResponse.getTotalPersonalAttributeList();           
						if (pal.isEmpty())
							pal = authnResponse.getPersonalAttributeList();
					}
					else
					{
						Monitoring.monitoringLog( "<span class='error'>Step 4: Error! Saml Response validation failed!</span>");
						exception = new ApplicationSpecificServiceException("Saml Response validation failed!", "Either the Issuer or the SAML response ID are invalid!");
					}
				}
			}catch(STORKSAMLEngineException e){	
				Monitoring.monitoringLog( "<span class='error'>Step 4: Error! Could not validate token for Saml Response</span>");
				exception = new ApplicationSpecificServiceException("Could not validate token for Saml Response", e.getErrorMessage());
			}
		} catch(Exception e) {
			Monitoring.monitoringLog( "<span class='error'>Step 4: Error! Saml Response is invalid!</span>");
			exception = new ApplicationSpecificServiceException("Saml Response is invalid!", "Failed to parse the SAML response from PEPS.");
		}

		//Check if we had a failure and throw an exception
		if ( exception!=null ) {
			Monitoring.monitoringLog( "<span class='error'>Step 4: Error!</span>");
			logger.error(exception.getMessage());
			return Action.ERROR;
			//throw exception;
		}
		Monitoring.monitoringLog( "<span class='success'>Step 4: Success!</span>");
		return Action.SUCCESS;
	}

	/**
	 * Validates a given {@link STORKAuthnResponse}.
	 * 
	 * @param authnResponse The {@link STORKAuthnResponse} to validate.
	 * 
	 * @return true if all tests are OK
	 */
	private boolean validateResponse(final STORKAuthnResponse attrResponse) {
		boolean outcome = false;

		if (getSession() != null) {
			final String sessionIdRequest = attrResponse.getInResponseTo();
			final String sessionIdActual = (String) getSession().getAttribute(Constants.SAML_IN_RESPONSE_TO_SP);

			final String audienceRestriction = attrResponse.getAudienceRestriction();
			final String issuer = (String) getSession().getAttribute(Constants.ISSUER_SP);

			if ( sessionIdActual == null || issuer == null ) {
				getSession().invalidate();
			}
			else {
				if (sessionIdRequest != null
						&& sessionIdActual.equals(sessionIdRequest)
						&& audienceRestriction != null && issuer.equals(audienceRestriction)) {
					outcome = true;
				}
			}
		}

		return outcome;
	}

	/**
	 * Setter for SAMLResponse.
	 * 
	 * @param SAMLResponse the SAMLResponse to set.
	 */
	public void setSAMLResponse(final String SAMLResponse) {
		this.SAMLResponse = SAMLResponse;
	}
	
	/**
	 * Getter for SAMLResponse.
	 * 
	 * @return the SAMLResponse value.
	 */
	public String getSAMLResponse() {
		return SAMLResponse;
	}

	/**
	 * Getter for the PAL.
	 * 
	 * @return The PAL.
	 */
	public IPersonalAttributeList getPersonalAttributeList() {
		return pal;
	}

	/**
	 * Setter for the PAL.
	 * 
	 * @param pal The PAL.
	 */
	public void setPersonalAttributeList(IPersonalAttributeList pal) {
		this.pal = pal;
	}
	
	/**
	 * Getter for the returnUrl.
	 * 
	 * @return The returnUrl.
	 */
	public String getFailUrl() {
		return failUrl;
	}

	/**
	 * Setter for the returnUrl
	 * 
	 * @param returnUrl The returnUrl.
	 */
	public void setFailUrl(String returnUrl) {
		this.failUrl = failUrl;
	}
}