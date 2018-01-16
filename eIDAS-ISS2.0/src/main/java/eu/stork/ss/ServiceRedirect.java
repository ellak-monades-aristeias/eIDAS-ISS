package eu.stork.ss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;
import com.opensymphony.xwork2.Action;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.PEPSUtil;
import eu.stork.peps.auth.commons.PersonalAttribute;
import eu.stork.peps.auth.commons.PersonalAttributeList;
import eu.stork.peps.auth.commons.STORKAuthnResponse;
import eu.stork.peps.auth.engine.STORKSAMLEngine;
import eu.stork.peps.exceptions.SAMLEngineException;
import eu.stork.peps.exceptions.STORKSAMLEngineException;
import eu.eidas.sp.SpAuthenticationResponseData;
import eu.eidas.sp.SpEidasSamlTools;
import eu.eidas.sp.SpProtocolEngineFactory;
import eu.eidas.sp.SpProtocolEngineI;
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
		
		//decode SAML Response:
		String samlType = (String)getSession().getAttribute("saml");
		getSession().removeAttribute("saml");
		if (samlType != null && samlType.equals("eIDAS"))
		{
			SpAuthenticationResponseData data = SpEidasSamlTools.processResponse(SAMLResponse, getServletRequest().getRemoteHost());
			
			if ( validateResponse(data) )
			{
	        	pal = eIDAS2PAL(data);
			}
			else
			{
				Monitoring.monitoringLog( "<span class='error'>Step 4: Error! Saml Response validation failed!</span>");
				exception = new ApplicationSpecificServiceException("Saml Response validation failed!", "Either the Issuer or the SAML response ID are invalid!");
			}
	        System.out.println("-----------");
		}
		else {
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
	
	private boolean validateResponse(final SpAuthenticationResponseData attrResponse) {
		boolean outcome = false;

		if (getSession() != null) {
			final String sessionIdRequest = attrResponse.getResponseToID();
			final String sessionIdActual = (String) getSession().getAttribute(Constants.SAML_IN_RESPONSE_TO_SP);

			final String audienceRestriction = attrResponse.getAudienceRestriction();
			final String issuer = (String) getSession().getAttribute(Constants.ISSUER_SP);
System.out.println("ISSPlus >>>>>>>>>>>> "+sessionIdActual +"-"+sessionIdRequest);
System.out.println("ISSPlus ------------ "+issuer +"-"+audienceRestriction);

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
	
	private IPersonalAttributeList eIDAS2PAL(SpAuthenticationResponseData data)
	{
		ArrayList<String []> eidasPal = data.getAttributes();
		IPersonalAttributeList pal = new PersonalAttributeList();
		
		System.out.println("ISSPlus STATUS CODE: ["+data.getStatusCode()+"]");
		if (!data.getStatusCode().equals("urn:oasis:names:tc:SAML:2.0:status:Success"))
		{
			PersonalAttribute pa = new PersonalAttribute();
 			pa.setName("StatusCode");
 			pa.setIsRequired(false);
 			pa.addValue(data.getStatusCode());
 			pal.add(pa);
 			
 			pa = new PersonalAttribute();
 			pa.setName("StatusMessage");
 			pa.setIsRequired(false);
 			pa.addValue(data.getStatusMessage());
 			pal.add(pa);
			
		}
		else 
		for (String []sp : eidasPal)
    	{
    		String attrName = sp[0];
    		boolean mandatory = Boolean.parseBoolean(sp[1]);
    		
    		String val = "null";
    		//for (int i = 2; i < sp.length; i++) val
    		if (sp.length > 2) val = sp[2];
    		System.out.println("ISSPlus\t"+attrName+"	"+val.toString());
    		
    		PersonalAttribute pa = new PersonalAttribute();
 			pa.setName(attrName);
 			pa.setIsRequired(mandatory);
 			pa.addValue(val);
 			pal.add(pa);
    	}
    	return pal;
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