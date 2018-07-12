<%@taglib uri="/struts-tags" prefix="s"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title><s:property value="%{getText('tituloId')}" /></title>
		<link href="css/estilos.css" rel="stylesheet" type="text/css" />
	</head>

	<body onload="document.redirectForm.submit();">
		<s:form name="redirectForm" method="post" action="%{spepsUrl}">
			<s:hidden name="SAMLRequest" value="%{samlToken}" />
		</s:form>

		<noscript>
			<div id="contenedor">
				<div id="cabecera">
					<div class="logo"></div>
					<div class="logo_ue"></div>
					<div class="tituloCabecera"><s:property value="%{getText('tituloCabeceraId')}" /></div>
				</div>

				<div id="borde">
					<div id="principal">
						<div id="margen">
							<h2>
								<s:i18n name="eu.stork.peps.bundle">
									<s:text name="RedirectToSPEPS" />
								</s:i18n>
							</h2>
				
							<br />

							<s:form name="submitForm" method="post"
								action="%{spepsUrl}">
								<s:hidden name="SAMLRequest" value="%{samlToken}" />
								<s:submit label="%{getText('accept.continue')}" />
							</s:form>						
						</div>
					</div>
				</div>
			</div>
		</noscript>
	</body>
</html>