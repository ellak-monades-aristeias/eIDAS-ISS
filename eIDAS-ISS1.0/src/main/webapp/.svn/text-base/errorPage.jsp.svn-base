<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title><s:property value="%{getText('tituloId')}" /></title>
<script type="text/javascript" src="js/script.js"></script>
<link href="css/estilos.css" rel="stylesheet" type="text/css" />
</head>
<body>

	<div id="contenedor">
		<div id="cabecera">
			<div class="logo"></div>
			<div class="tituloCabecera">
				<s:property value="%{getText('tituloCabeceraId')}" />
			</div>
		</div>
		<div id="borde">
			<div id="principal">
				<div id="margen">
					<h1>
						<s:property value="%{getText('errorId')}" />
					</h1>
					<h2>
						<s:property value="%{exception.title}" />
					</h2>
					<div id="space"></div>
					<s:actionerror />
					<s:property value="%{exception.message}" />

					<p>
						<s:property value="%{getText('errorMessage1Id')}" />
						<a href="<s:property value="%{getText('errorHomepage')}"/>"><s:property
								value="%{getText('errorMessage2Id')}" /></a>
						<s:property value="%{getText('errorMessage3Id')}" />
					</p>

				</div>
			</div>
		</div>
	</div>

</body>
</html>
