(function () {
  'use strict';

  function formatSize(bytes) {
    if (bytes < 1024) {
      return bytes + 'B';
    }
    if (bytes < 1024 * 1024) {
      return Math.round(bytes / 1024) + 'KB';
    }
    return (bytes / (1024 * 1024)).toFixed(1) + 'MB';
  }

  function extensionOf(name) {
    var dot = name.lastIndexOf('.');
    return dot < 0 ? '' : name.substring(dot + 1).toLowerCase();
  }

  /* 삭제 등 되돌릴 수 없는 요청 전에 확인을 받는다 */
  function initConfirm() {
    document.addEventListener('click', function (event) {
      var target = event.target.closest('[data-confirm]');
      if (target && !window.confirm(target.getAttribute('data-confirm'))) {
        event.preventDefault();
      }
    });
  }

  function initTextareaCount() {
    document.querySelectorAll('[data-count-for]').forEach(function (counter) {
      var textarea = document.getElementById(counter.getAttribute('data-count-for'));
      if (!textarea) {
        return;
      }
      var update = function () {
        counter.textContent = String(textarea.value.length);
      };
      textarea.addEventListener('input', update);
      update();
    });
  }

  /* KRDS 파일 업로드: 선택 파일 목록 표시, 개별 제거, 끌어다 놓기, 형식·용량·개수 사전 검사 */
  function initFileUpload(root) {
    var input = root.querySelector('input[type="file"]');
    var selectButton = root.querySelector('[data-file-select]');
    var dropZone = root.querySelector('[data-drop-zone]');
    var list = root.querySelector('[data-file-list]');
    var total = root.querySelector('[data-file-total]');
    var errorBox = root.querySelector('[data-file-error]');
    var accept = (root.getAttribute('data-accept') || '').split(',').map(function (ext) {
      return ext.replace('.', '').trim().toLowerCase();
    }).filter(Boolean);
    var maxCount = parseInt(root.getAttribute('data-max-count'), 10) || 10;
    var maxSize = parseInt(root.getAttribute('data-max-size'), 10) || 0;
    var existingCount = parseInt(root.getAttribute('data-existing-count'), 10) || 0;
    var selected = [];

    function showError(messages) {
      if (messages.length === 0) {
        errorBox.hidden = true;
        errorBox.textContent = '';
        return;
      }
      errorBox.hidden = false;
      errorBox.textContent = messages.join(' ');
    }

    function syncInput() {
      var transfer = new DataTransfer();
      selected.forEach(function (file) {
        transfer.items.add(file);
      });
      input.files = transfer.files;
    }

    function render() {
      list.innerHTML = '';
      selected.forEach(function (file, index) {
        var item = document.createElement('li');
        var info = document.createElement('div');
        info.className = 'file-info';

        var name = document.createElement('div');
        name.className = 'file-name';
        name.textContent = file.name + ' [' + (extensionOf(file.name) || '-') + ', ' + formatSize(file.size) + ']';

        var buttons = document.createElement('div');
        buttons.className = 'btn-wrap';
        var remove = document.createElement('button');
        remove.type = 'button';
        remove.className = 'krds-btn medium text';
        remove.setAttribute('title', file.name + ' 선택 취소');
        remove.appendChild(document.createTextNode('삭제 '));
        var icon = document.createElement('i');
        icon.className = 'svg-icon ico-delete-fill';
        icon.setAttribute('aria-hidden', 'true');
        remove.appendChild(icon);
        remove.addEventListener('click', function () {
          selected.splice(index, 1);
          syncInput();
          render();
          showError([]);
          selectButton.focus();
        });

        buttons.appendChild(remove);
        info.appendChild(name);
        info.appendChild(buttons);
        item.appendChild(info);
        list.appendChild(item);
      });
      total.textContent = (existingCount + selected.length) + '개';
    }

    function addFiles(files) {
      var errors = [];
      Array.prototype.forEach.call(files, function (file) {
        var ext = extensionOf(file.name);
        if (accept.length > 0 && accept.indexOf(ext) < 0) {
          errors.push(file.name + ': 첨부할 수 없는 파일 형식입니다.');
        } else if (maxSize > 0 && file.size > maxSize) {
          errors.push(file.name + ': 파일당 ' + formatSize(maxSize) + ' 이하만 첨부할 수 있습니다.');
        } else if (file.size === 0) {
          errors.push(file.name + ': 빈 파일은 첨부할 수 없습니다.');
        } else if (existingCount + selected.length >= maxCount) {
          errors.push(file.name + ': 게시물당 최대 ' + maxCount + '개까지 첨부할 수 있습니다.');
        } else {
          selected.push(file);
        }
      });
      syncInput();
      render();
      showError(errors);
    }

    selectButton.addEventListener('click', function () {
      input.click();
    });

    input.addEventListener('change', function () {
      var files = Array.prototype.slice.call(input.files);
      addFiles(files);
    });

    ['dragenter', 'dragover'].forEach(function (type) {
      dropZone.addEventListener(type, function (event) {
        event.preventDefault();
        dropZone.classList.add('is-dragover');
      });
    });
    ['dragleave', 'drop'].forEach(function (type) {
      dropZone.addEventListener(type, function (event) {
        event.preventDefault();
        dropZone.classList.remove('is-dragover');
      });
    });
    dropZone.addEventListener('drop', function (event) {
      if (event.dataTransfer && event.dataTransfer.files) {
        addFiles(event.dataTransfer.files);
      }
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    initConfirm();
    initTextareaCount();
    document.querySelectorAll('[data-file-upload]').forEach(initFileUpload);
  });
})();
