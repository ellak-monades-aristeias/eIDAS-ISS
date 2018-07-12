package eu.stork.ss;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.Action;

import eu.stork.peps.auth.commons.IPersonalAttributeList;

public abstract class SavePersonalAttributeList extends AbstractAction {
	/**
	 * Unique identifier.
	 */
	private static final long serialVersionUID = 3013322052837638394L;

	//The logger
	static final Logger logger = LoggerFactory.getLogger(SavePersonalAttributeList.class.getName());

	//The PAL
	private IPersonalAttributeList pal;

	//The token
	private String returnUrl;

	/**
	 * Check that the session variables are OK and display the list of known countries
	 * for the user to select. The selected country must be the users home CPEPS.
	 */
	public String execute() {
		HttpSession session;
		String token;
		session = this.getSession();

		//Check that the session contains valid information
		synchronized(session) {
			token = (String)session.getAttribute(Constants.SP_TOKEN);

			if ( session==null || token==null || pal==null ) {
				String message = "Session is empty or contains invalid data!";

				logger.error(message);
				throw new ApplicationSpecificServiceException("Session error", message);
			}
		}

		returnUrl = savePersonalAttributeList(token, pal);
		if ( returnUrl==null ) {
			String message = "Failed to save the PAL returned from STORK to the SP.";

			logger.error(message);
			throw new ApplicationSpecificServiceException("PAL saving failed!", message);
		}

		return Action.SUCCESS;
	}

	/**
	 * Save the PAL and return the outcome.
	 * 
	 * @param pal The PAL to save.
	 * 
	 * @return The URL to redirect the user or null if an error occurred.
	 */
	protected abstract String savePersonalAttributeList(String token, IPersonalAttributeList pal);

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
	public String getReturnUrl() {
		return returnUrl;
	}

	/**
	 * Setter for the returnUrl
	 * 
	 * @param returnUrl The returnUrl.
	 */
	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}
}