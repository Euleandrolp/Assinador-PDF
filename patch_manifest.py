# Faz o app aparecer em "Abrir com" / "Compartilhar" para arquivos PDF
p = 'android/app/src/main/AndroidManifest.xml'
s = open(p, encoding='utf-8').read()
filtros = '''
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:mimeType="application/pdf" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:scheme="content" />
                <data android:scheme="file" />
                <data android:mimeType="application/pdf" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="application/pdf" />
            </intent-filter>
        '''
if 'android.intent.action.SEND' not in s:
    i = s.index('</activity>')
    s = s[:i] + filtros + s[i:]
if 'launchMode' not in s:
    s = s.replace('android:name=".MainActivity"', 'android:name=".MainActivity"\n            android:launchMode="singleTask"', 1)
open(p, 'w', encoding='utf-8').write(s)
print('AndroidManifest atualizado')
