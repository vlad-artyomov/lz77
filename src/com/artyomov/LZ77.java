package com.artyomov;

import java.io.*;

public class LZ77 {
    public static final int DEFAULT_BUFF_SIZE = 1024;
    protected int mBufferSize;
    protected Reader mIn;
    protected PrintWriter mOut;
    protected StringBuffer mSearchBuffer;

    public LZ77() {
        this(DEFAULT_BUFF_SIZE);
    }

    public LZ77(int buffSize) {
        mBufferSize = buffSize;
        mSearchBuffer = new StringBuffer(mBufferSize);
    }

    private void trimSearchBuffer() {
        if (mSearchBuffer.length() > mBufferSize) {
            mSearchBuffer =
                    mSearchBuffer.delete(0, mSearchBuffer.length() - mBufferSize);
        }
    }

    public void unCompress(String infile) throws IOException {
        mIn = new BufferedReader(new FileReader(infile.substring(0, infile.lastIndexOf('.')) + ".lz77"));
        StreamTokenizer st = new StreamTokenizer(mIn);

        st.ordinaryChar((int) ' ');
        st.ordinaryChar((int) '.');
        st.ordinaryChar((int) '-');
        st.ordinaryChar((int) '\n');
        st.wordChars((int) '\n', (int) '\n');
        st.wordChars((int) ' ', (int) '}');

        int offset, length;
        while (st.nextToken() != StreamTokenizer.TT_EOF) {
            switch (st.ttype) {
                case StreamTokenizer.TT_WORD:
                    mSearchBuffer.append(st.sval);
                    //System.out.print(st.sval);
                    //Меняем размер буффера если требуется
                    trimSearchBuffer();
                    break;
                case StreamTokenizer.TT_NUMBER:
                    offset = (int) st.nval; // устанавливаем отступ
                    st.nextToken(); //получаем разделитель (если он есть после цифры)
                    if (st.ttype == StreamTokenizer.TT_WORD) {
                        // если получаем слово, вместо разделителя
                        // тогда цифры идут как слова
                        mSearchBuffer.append(offset + st.sval);
                        //System.out.print(offset+st.sval);
                        break;
                    }
                    // если был разделитель, то получаем длину после отступа
                    st.nextToken(); // длина
                    length = (int) st.nval;
                    //получаем строку из буфера поиска
                    String output = "";
                    try {
                        output = mSearchBuffer.substring(offset, offset + length);
                    } catch (StringIndexOutOfBoundsException e) {
                        System.err.println("Что-то пошло не так :(");
                    }
                    //System.out.print(output);
                    mSearchBuffer.append(output);
                    ///Меняем размер буффера если требуется
                    trimSearchBuffer();
                    break;
                default:
                    //не учитываем разделитель ~
            }
        }
        mIn.close();
    }

    public void compress(String infile) throws IOException {
        // исходный и результирующий файлы
        mIn = new BufferedReader(new FileReader(infile));
        mOut = new PrintWriter(new BufferedWriter(new FileWriter(infile.substring(0, infile.lastIndexOf('.')) + ".lz77")));

        int nextChar;
        String currentMatch = "";
        int matchIndex = 0, tempIndex = 0;

        //читаем пока есть символы в файле
        while ((nextChar = mIn.read()) != -1) {
            // ищем совпадение в словаре
            tempIndex = mSearchBuffer.indexOf(currentMatch + (char) nextChar);
            // если есть совпадение, то добавляем к текущему след символ и обновляем индекс совпадения
            if (tempIndex != -1) {
                currentMatch += (char) nextChar;
                matchIndex = tempIndex;
            } else {
                //кодируем найбольшее совпадение
                String codedString =
                        "~" + matchIndex + "~" + currentMatch.length() + "~" + (char) nextChar;
                String concat = currentMatch + (char) nextChar;
                //проверяем, что строка больше закодированной
                if (codedString.length() <= concat.length()) {
                    mOut.print(codedString);
                    mSearchBuffer.append(concat); //добавляем в словарь
                    currentMatch = "";
                    matchIndex = 0;
                } else {
                    //просто добавляем сиволы как они есть, не кодируя
                    currentMatch = concat;
                    matchIndex = -1;
                    while (currentMatch.length() > 1 && matchIndex == -1) {
                        mOut.print(currentMatch.charAt(0));
                        mSearchBuffer.append(currentMatch.charAt(0));
                        currentMatch = currentMatch.substring(1, currentMatch.length());
                        matchIndex = mSearchBuffer.indexOf(currentMatch);
                    }
                }
                //меняем размер буфера, если требуется
                trimSearchBuffer();
            }
        }

        //если конец файла добавляем последние символы
        if (matchIndex != -1) {
            String codedString =
                    "~" + matchIndex + "~" + currentMatch.length();
            if (codedString.length() <= currentMatch.length()) {
                mOut.print("~" + matchIndex + "~" + currentMatch.length());
            } else {
                mOut.print(currentMatch);
            }
        }

        // закрываем файлы
        mIn.close();
        mOut.flush();
        mOut.close();
    }
}