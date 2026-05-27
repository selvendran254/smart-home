/**
 * Smart Home — browser Text-to-Speech via SpeechSynthesis API.
 * Call speakResponse(plainText) after each AI / Plant Doctor reply.
 */
(function (global) {
    'use strict';

    var LS = {
        enabled: 'smarthome_tts_enabled',
        voiceUri: 'smarthome_tts_voice_uri',
        rate: 'smarthome_tts_rate',
        pitch: 'smarthome_tts_pitch',
        panelExpanded: 'smarthome_tts_panel_expanded'
    };

    var ROOT_ID = 'smarthome-tts-root';
    var mounted = false;

    function isEnabled() {
        var v = localStorage.getItem(LS.enabled);
        if (v === null || v === '') return true;
        return v === 'true';
    }

    function setEnabled(on) {
        localStorage.setItem(LS.enabled, on ? 'true' : 'false');
        syncToggleButtons(on);
        if (!on) stopSpeech();
    }

    function syncToggleButtons(on) {
        document.querySelectorAll('[data-smart-home-tts-toggle]').forEach(function (btn) {
            btn.setAttribute('aria-pressed', on ? 'true' : 'false');
            btn.textContent = on ? 'Speech: ON' : 'Speech: OFF';
            btn.classList.toggle('tts-toggle-off', !on);
        });
    }

    /** Strip HTML / light Markdown for natural TTS. */
    function cleanTextForSpeech(raw) {
        if (raw == null) return '';
        var s = String(raw);
        try {
            var div = document.createElement('div');
            div.innerHTML = s;
            s = div.textContent || div.innerText || s;
        } catch (e) { /* ignore */ }
        s = s
            .replace(/\*\*(.*?)\*\*/g, '$1')
            .replace(/\*(.*?)\*/g, '$1')
            .replace(/^#{1,6}\s+/gm, '')
            .replace(/`+/g, '')
            .replace(/\[(.*?)\]\([^)]*\)/g, '$1')
            .replace(/\s+/g, ' ')
            .trim();
        return s.slice(0, 16000);
    }

    function stopSpeech() {
        if ('speechSynthesis' in global) {
            try {
                global.speechSynthesis.cancel();
            } catch (e) {
                console.warn('speechSynthesis.cancel', e);
            }
        }
    }

    /**
     * Speak assistant response. Cancels any ongoing speech first.
     * @param {string} responseText — plain or HTML-ish; will be cleaned.
     */
    function speakResponse(responseText) {
        if (!('speechSynthesis' in global)) {
            console.warn('Speech synthesis not supported in this browser.');
            return;
        }
        if (!isEnabled()) return;

        var text = cleanTextForSpeech(responseText);
        if (!text) return;

        stopSpeech();

        var u = new SpeechSynthesisUtterance(text);
        u.rate = clamp(parseFloat(localStorage.getItem(LS.rate) || '1'), 0.5, 2);
        u.pitch = clamp(parseFloat(localStorage.getItem(LS.pitch) || '1'), 0, 2);
        u.lang = 'en-IN';

        var uri = localStorage.getItem(LS.voiceUri);
        var voices = global.speechSynthesis.getVoices();
        if (uri && voices && voices.length) {
            var match = voices.filter(function (v) { return v.voiceURI === uri; })[0];
            if (match) u.voice = match;
        }

        u.onerror = function (ev) {
            console.warn('SpeechSynthesisUtterance error:', ev.error || ev);
        };

        try {
            global.speechSynthesis.speak(u);
        } catch (e) {
            console.warn('speechSynthesis.speak failed', e);
        }
    }

    function clamp(n, lo, hi) {
        if (isNaN(n)) return lo;
        return Math.min(hi, Math.max(lo, n));
    }

    function populateVoiceSelect(select) {
        if (!select) return;
        var voices = global.speechSynthesis.getVoices() || [];
        var saved = localStorage.getItem(LS.voiceUri);
        select.innerHTML = '';
        if (!voices.length) {
            select.innerHTML = '<option value="">Loading voices…</option>';
            return;
        }
        voices.forEach(function (v) {
            var opt = document.createElement('option');
            opt.value = v.voiceURI;
            opt.textContent = v.name + ' (' + v.lang + ')';
            select.appendChild(opt);
        });
        if (saved) {
            var ok = voices.some(function (v) { return v.voiceURI === saved; });
            if (ok) select.value = saved;
        } else {
            var prefer = voices.filter(function (v) {
                return /^en(-|$)/i.test(v.lang || '');
            })[0];
            if (prefer) {
                select.value = prefer.voiceURI;
                localStorage.setItem(LS.voiceUri, prefer.voiceURI);
            }
        }
    }

    function createPanelStyles() {
        if (document.getElementById('smart-home-tts-styles')) return;
        var css = document.createElement('style');
        css.id = 'smart-home-tts-styles';
        css.textContent =
            '.smart-home-tts-root{margin-top:12px;padding:0;border-radius:12px;background:rgba(15,23,42,.65);border:1px solid #334155;text-align:left;overflow:hidden}' +
            'button.smart-home-tts-expand{width:100%;margin:0;padding:12px 14px;border:none;background:transparent;color:#94a3b8;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:.04em;cursor:pointer;display:flex;align-items:center;justify-content:space-between;gap:10px;text-align:left;font-family:inherit;transition:background .15s,color .15s}' +
            'button.smart-home-tts-expand:hover{background:rgba(51,65,85,.45);color:#e2e8f0}' +
            'button.smart-home-tts-expand:focus-visible{outline:2px solid #22c55e;outline-offset:-2px}' +
            '.smart-home-tts-expand-label{display:flex;align-items:center;gap:8px}' +
            '.smart-home-tts-chevron{font-size:10px;opacity:.85}' +
            '.smart-home-tts-body{padding:0 12px 12px}' +
            '.smart-home-tts-row{display:flex;flex-wrap:wrap;align-items:center;gap:8px;margin-bottom:10px}' +
            '.smart-home-tts-row:last-child{margin-bottom:0}' +
            '.smart-home-tts-root label{font-size:12px;color:#94a3b8;min-width:52px}' +
            '.smart-home-tts-root select{flex:1;min-width:0;padding:8px 10px;border-radius:8px;border:1px solid #475569;background:#1e293b;color:#f1f5f9;font-size:13px}' +
            '.smart-home-tts-root input[type=range]{flex:1;min-width:100px;accent-color:#22c55e}' +
            '.smart-home-tts-root .tts-val{font-size:12px;color:#cbd5e1;width:36px;text-align:right}' +
            'button[data-smart-home-tts-toggle]{width:100%;padding:10px 12px;border:none;border-radius:10px;background:#22c55e;color:#fff;font-weight:700;font-size:14px;cursor:pointer;transition:background .15s}' +
            'button[data-smart-home-tts-toggle]:hover{background:#16a34a}' +
            'button[data-smart-home-tts-toggle].tts-toggle-off{background:#475569}' +
            'button[data-smart-home-tts-toggle].tts-toggle-off:hover{background:#64748b}' +
            'body.light-mode .smart-home-tts-root{background:#f1f5f9;border-color:#cbd5e1}' +
            'body.light-mode .smart-home-tts-root select{background:#fff;color:#0f172a;border-color:#cbd5e1}' +
            'body.light-mode button.smart-home-tts-expand{color:#64748b}' +
            'body.light-mode button.smart-home-tts-expand:hover{background:#e2e8f0;color:#0f172a}' +
            'body.light-mode .smart-home-tts-root label{color:#64748b}' +
            'body.light-mode .smart-home-tts-root .tts-val{color:#475569}';
        document.head.appendChild(css);
    }

    function createPanel() {
        createPanelStyles();
        var root = document.createElement('div');
        root.id = ROOT_ID;
        root.className = 'smart-home-tts-root';
        root.innerHTML =
            '<button type="button" class="smart-home-tts-expand" aria-expanded="false" aria-controls="smarthome-tts-panel-body" id="smarthome-tts-expand-btn">' +
            '<span class="smart-home-tts-expand-label"><span aria-hidden="true">🔊</span> Speech output</span>' +
            '<span class="smart-home-tts-chevron" aria-hidden="true">▶</span>' +
            '</button>' +
            '<div class="smart-home-tts-body" id="smarthome-tts-panel-body" hidden>' +
            '<div class="smart-home-tts-row">' +
            '<button type="button" data-smart-home-tts-toggle="" aria-pressed="true">Speech: ON</button>' +
            '</div>' +
            '<div class="smart-home-tts-row">' +
            '<label for="smarthome-tts-voice">Voice</label>' +
            '<select id="smarthome-tts-voice" aria-label="Speech voice"></select>' +
            '</div>' +
            '<div class="smart-home-tts-row">' +
            '<label for="smarthome-tts-rate">Rate</label>' +
            '<input type="range" id="smarthome-tts-rate" min="0.5" max="2" step="0.1" value="1" aria-label="Speech rate">' +
            '<span class="tts-val" id="smarthome-tts-rate-val">1</span>' +
            '</div>' +
            '<div class="smart-home-tts-row">' +
            '<label for="smarthome-tts-pitch">Pitch</label>' +
            '<input type="range" id="smarthome-tts-pitch" min="0" max="2" step="0.1" value="1" aria-label="Speech pitch">' +
            '<span class="tts-val" id="smarthome-tts-pitch-val">1</span>' +
            '</div>' +
            '</div>';

        var expandBtn = root.querySelector('#smarthome-tts-expand-btn');
        var panelBody = root.querySelector('#smarthome-tts-panel-body');
        var chevron = root.querySelector('.smart-home-tts-chevron');

        function applyPanelExpanded(expanded) {
            expandBtn.setAttribute('aria-expanded', expanded ? 'true' : 'false');
            panelBody.hidden = !expanded;
            chevron.textContent = expanded ? '▼' : '▶';
            localStorage.setItem(LS.panelExpanded, expanded ? 'true' : 'false');
        }

        var savedExpanded = localStorage.getItem(LS.panelExpanded);
        applyPanelExpanded(savedExpanded === 'true');

        expandBtn.addEventListener('click', function () {
            var open = expandBtn.getAttribute('aria-expanded') !== 'true';
            applyPanelExpanded(open);
        });

        var toggle = root.querySelector('[data-smart-home-tts-toggle]');
        toggle.addEventListener('click', function () {
            var next = !isEnabled();
            setEnabled(next);
        });

        var voiceSel = root.querySelector('#smarthome-tts-voice');
        voiceSel.addEventListener('change', function () {
            localStorage.setItem(LS.voiceUri, voiceSel.value);
        });

        var rate = root.querySelector('#smarthome-tts-rate');
        var rateVal = root.querySelector('#smarthome-tts-rate-val');
        var savedRate = localStorage.getItem(LS.rate);
        if (savedRate != null) rate.value = savedRate;
        rateVal.textContent = rate.value;
        rate.addEventListener('input', function () {
            rateVal.textContent = rate.value;
            localStorage.setItem(LS.rate, rate.value);
        });

        var pitch = root.querySelector('#smarthome-tts-pitch');
        var pitchVal = root.querySelector('#smarthome-tts-pitch-val');
        var savedPitch = localStorage.getItem(LS.pitch);
        if (savedPitch != null) pitch.value = savedPitch;
        pitchVal.textContent = pitch.value;
        pitch.addEventListener('input', function () {
            pitchVal.textContent = pitch.value;
            localStorage.setItem(LS.pitch, pitch.value);
        });

        function refreshVoices() {
            populateVoiceSelect(voiceSel);
        }

        global.speechSynthesis.onvoiceschanged = refreshVoices;
        refreshVoices();
        setTimeout(refreshVoices, 400);

        syncToggleButtons(isEnabled());

        return root;
    }

    /** Mount panel into #tts-mount-sidebar or #tts-mount-voice (first match). Call once. */
    function mountSpeechPanel() {
        if (mounted || document.getElementById(ROOT_ID)) {
            mounted = true;
            return;
        }
        var host = document.getElementById('tts-mount-sidebar') || document.getElementById('tts-mount-voice');
        if (!host) return;
        host.appendChild(createPanel());
        mounted = true;
    }

    global.smartHomeSpeakResponse = speakResponse;
    global.smartHomeStopSpeech = stopSpeech;

    global.SmartHomeSpeech = {
        speakResponse: speakResponse,
        stop: stopSpeech,
        cleanTextForSpeech: cleanTextForSpeech,
        mount: mountSpeechPanel,
        isEnabled: isEnabled
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', mountSpeechPanel);
    } else {
        mountSpeechPanel();
    }

})(window);
