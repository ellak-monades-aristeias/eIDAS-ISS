<%@taglib uri="/struts-tags" prefix="s"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><s:property value="%{getText('tituloId')}" /></title>
    <link href="css/estilos.css" rel="stylesheet" type="text/css" />
    
    <link rel="stylesheet" href="select2/select2.css" type="text/css" media="screen" />
	<script type="text/javascript" src="js/jquery-1.11.0.min.js"></script>
	<script type="text/javascript" src="select2/select2.min.js"></script>
    <script type="text/javascript">
	    function format(state) {
    		if (!state.id) return state.text; // optgroup
    		return "<img class='flag' src='img/banderas/" + state.id.toUpperCase() + ".gif'/>" + state.text;
    	}

	    $(document).ready(function() {
	    	$("#cpeps").select2({
	    		 formatResult: format,
	    		 formatSelection: format,
	    		 escapeMarkup: function(m) { return m; }
	    	});
	    });
    </script>
	<style>
		img.flag {
			height: 10px;
		    padding-right: 10px;
    		width: 15px;
		}
		.error {
			border-radius: 10px;
			background: none repeat scroll 0 0 #FFC0CB;
			border: 2px solid #FF0000;
			font-weight: bold;
			margin-bottom: 10px;
			padding: 20px 10px;
		}
	</style>
  </head>

  <body>
    <div id="contenedor">

      <div id="cabecera">
        <div class="logo"></div>
        <div class="logo_ue"></div>
        <div class="tituloCabecera"><s:property value="%{getText('tituloCabeceraId')}" /></div>
      </div>
      
      <div id="borde">
        <div id="principal">
          <div id="margen">

            <h2 style="padding: 20px 0px 10px 0px;">
              <s:i18n name="eu.stork.ss.bundle">
                <s:text name="CountrySelectAction" />
              </s:i18n>
            </h2>

			<s:if test="errorFound == 1">
	            <h2 class="error">
	              <s:i18n name="eu.stork.ss.bundle">
	                <s:text name="ErrorCountrySelectAction" />
	              </s:i18n>
	            </h2>
			</s:if>

            <form name="cpepsSelector" method="post" action="ValidateSelection">

				<div id="designhtml">
					<select name="cpeps" id="cpeps" style="width: 140px;">
						<s:iterator value="countries">
							<option value="<s:property value="name"/>">
								<s:property value="name" />
							</option>
						</s:iterator>
					</select>
				</div>
              <div style="padding-top: 20px;"><s:submit value="%{getText('accept.send')}" /></div>

            </form>
          </div>
        </div>
      </div>
    </div>

  </body>
</html>