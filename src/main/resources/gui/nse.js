//  -*- coding: utf-8-unix -*-
$(function() {
  var LOCAL_STORAGE_KEY = 'nablarch-sql-executor';
  var CONFIG = {};

  $('#list')
  .on('click', '.sqlid h4', show)
  .on('click', '.control button.exec' , exec)
  .on('click', '.control button.clear', clear)
  .on('click', '.control button.fill' , fill);

  $('#config')
  .on('click', '#reload', reload);

  init();

  function init() {
    CONFIG = JSON.parse(
      window.localStorage.getItem(LOCAL_STORAGE_KEY) || '{}'
    );
    $('#config input').val(CONFIG.searchRoot);
    $('#list').text('****** 検索中 *******');
    $.ajax({
      url      :'./api'
    , data     :{l:CONFIG.searchRoot || null}
    , dataType :'text'
    , complete :list
    });
  }

  function list(xhr, status) {
    var $list = $('#list').empty()
      , text  = xhr.responseText;
    if (status !== 'success') {
      $list.text('検索に失敗しました。: ' + text);
      return;
    }
    if (!text.has(/\S+/)) {
      $list.text('SQLファイルが存在しません。検索ルートを再設定してください。');
      return;
    }
    $list.append(
      text.split(/\r?\n/).map(render).compact().join('')
    );
  }

  function reload(event) {
    CONFIG.searchRoot = $('#config input').val();
    window.localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(CONFIG));
    window.location.reload();
  }

  function render(item) {
    if (!item) {
      return;
    }
    else if (item.startsWith('===')) {
      return '';
    }
    else if (item.endsWith('.sql')) {
      return '<h3>'+item+'</h3>';
    }
    else {
      return '<div class="sqlid" data-sqlid="' + item + '">'
           + '<h4>' + item.split('.')[1] + '</h4>'
           + '<div class="content"></div>'
           + '</div>';
    }
  }

  function clear(event) {
    var $button = $(event.currentTarget)
      , $target = $button.closest('.sqlid');

    $target.find('.screen').hide();
    $target.find('input').val('');
  }

  function fill(event) {
    var $button = $(event.currentTarget)
      , $target = $button.closest('.sqlid');

    $target.find('.indexedParam, .namedParam, .nablarchFunction').each(function() {
      var $input = $(this)
        , prev = window.localStorage.getItem($input.attr('name'));
      if (prev) {
        $input.val(prev);
      }
    });
  }

  function show(event) {
    var $title   = $(event.currentTarget)
      , $target  = $title.parent()
      , $content = $target.find('.content')
      , sqlid    = $target.attr('data-sqlid')
      , paramIndex = 0;

    if ($target.has('.sql').length > 0) {
      $content.slideToggle();
      return;
    }

    $.ajax({
      url      :'./api'
    , data     :{s:sqlid, r:CONFIG.searchRoot}
    , dataType :'text'
    , success  :function(sql) {
        $content.hide();
        $('<pre class="brush: sql">')
          .html(sql)
          .appendTo($content);
        $('<div class="control">'
        + '  <button class="exec" data-sqlid="'+sqlid+'">'
        + '  Run'
        + '  </button>'
        + '  <button class="fill" data-sqlid="'+sqlid+'">'
        + '  Fill'
        + '  </button>'
        + '  <button class="clear" data-sqlid="'+sqlid+'">'
        + '  Clear'
        + '  </button>'
       + '</div>'
        ).appendTo($content);
        $('<pre class="screen" style="display:none;"></pre>')
        .appendTo($content);
        // ここでclassが付与される（namedParam, indexedParamなど)
        SyntaxHighlighter.highlight();
        $content.slideDown();

        $target
        .find('.namedParam, .indexedParam, .nablarchFunction')
        .each(function() {
          var $this = $(this)
            , paramName = $this.text().trim()
            , name = (paramName === '?')
                   ? ++paramIndex
                   : paramName.replace(':', '')
            , headPercent = /^:%/
            , functionRegex = /^(\$[a-z]+\s*)\(([^\)]*)(\))$/
            , functionNameMatch = []

        // 前方に%がある場合(例: :%name）、この%を削除する。
          if (headPercent.test(paramName)) {
             paramName = paramName.replace(headPercent, ':');
             $(this).before('<code class="sql plain">%</code>')
          }
          // nablarch提供の関数
          if (functionRegex.test(paramName)) {
            functionNameMatch = paramName.match(functionRegex);
            paramName = functionNameMatch[2];
            $(this).before('<code class="sql color2">' + functionNameMatch[1] + '(</code>');
            $(this).after('<code class="sql color2">' + functionNameMatch[3] + '</code>');
          }

          $(this).replaceWith(
            $('<input>',
            { type  :'text'
            , class :(paramName === '?') ? 'indexedParam'
                                         : 'namedParam'
            , placeholder :paramName
            , name :sqlid + '_' + name
            , 'data-paramName': (paramName === '?') ? paramIndex
                                                    : paramName
            })
          );
        });
      }
    });
  }

  function exec(event) {
    var $button = $(event.currentTarget)
      , sqlid   = $button.attr('data-sqlid')
      , $target = $button.closest('.sqlid')
      , args    = [];

    $target.find('.namedParam, .indexedParam').each(function() {
      var $input = $(this)
        , name   = $input.attr('data-paramName')
        , value  = $input.val();

      window.localStorage.setItem($input.attr('name'), value);

      if ($input.is('.indexedParam')) {
        args.push(value);
      }
      else if ($input.is('.namedParam')) {
        args.push(name);
        args.push(value);
      }
    });


    $.ajax({
      url: './api'
    , dataType: 'text'
    , data: {e: sqlid, r: CONFIG.searchRoot, args: args}
    , traditional: true
    , success: render
    , error: showError
    });

    function render(text) {
      text = text.replace(/^[\s\S]*<<-+>>/, '');
      $target.find('pre.screen')
             .fadeOut().addClass('ok').text(text).fadeIn();
    }

    function showError(xhr) {
      var text = xhr.responseText.replace(/^[\s\S]*<<-+>>/, '');
      $target.find('pre.screen')
             .fadeOut().addClass('ng').text(text).fadeIn();
    }
  }
});
