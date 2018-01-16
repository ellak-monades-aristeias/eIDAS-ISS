<%@taglib uri="/struts-tags" prefix="s"%>
<html>
        <body onload="document.redirectForm.submit();">
                <s:form name="redirectForm" method="post" action="%{spepsUrl}">
                        <s:hidden name="SAMLRequest" value="%{samlToken}" />
                        <s:hidden id="country" name="country" value="%{citizenCountry}" />
                </s:form>
        </body>
</html>
