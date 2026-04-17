$(function() {
	plot = document.getElementById('plot');
	textresult = document.getElementById('textresult');


    const history_server = '/history';
    const historytext_server = '/historytext';
    const hyponyms_server = '/hyponyms';
    const answer_server = '/answer';

    function get_params() {
        return {
            words: document.getElementById('words').value,
            startYear: document.getElementById('start').value,
            endYear: document.getElementById('end').value,
            k: document.getElementById('k').value,

            useLLM: document.getElementById('use-llm').checked
        }
    }

    $('#history').click(historyButton);
    $('#historytext').click(historyTextButton);
    $('#hyponyms').click(hyponymsButton);

    $('#answer').click(answerButton);

    function historyButton() {
        $("#textresult").hide();
        $("#plot").show();

        var params = get_params();
        console.log(params);
        $.get({
            async: false,
            url: history_server,
            data: params,
            success: function(data) {
            	console.log(data)

                plot.src = 'data:image/png;base64,' + data;

            },
            error: function(data) {
            	console.log("error")
            	console.log(data);
            	plot.src = 'data:image/png;base64,' + data;
            },
            dataType: 'json'
        });
    }

    function historyTextButton() {
        console.log("history text call");
        $("#plot").hide();
        $("#textresult").show();

        var params = get_params();
        console.log(params);
        $.get({
            async: false,
            url: historytext_server,
            data: params,
            success: function(data) {
            	console.log(data)

                textresult.value = data;

            },
            error: function(data) {
            	console.log("error")
            	console.log(data);
            },
            dataType: 'json'
        });
    }

    function hyponymsButton() {
        console.log("hyponyms call");
        $("#plot").hide();
        $("#textresult").show();

        var params = get_params();
        console.log(params);
        $.get({
            async: false,
            url: hyponyms_server,
            data: params,
            success: function(data) {
                console.log(data)

                textresult.value = data;

            },
            error: function(data) {
                console.log("error")
                console.log(data);
            },
            dataType: 'json'
        });
    }


    function answerButton() {
        console.log("answer call");
        $("#plot").hide();
        $("#textresult").show();

        var params = get_params();
        console.log(params);

        $.get({
            async: false,
            url: answer_server,
            data: params,
            success: function(data) {
                console.log(data);
                textresult.value = data;
            },
            error: function(data) {
                console.log("error");
                console.log(data);
                textresult.value = "Error generating answer.";
            },
            dataType: 'json'
        });
    }

});