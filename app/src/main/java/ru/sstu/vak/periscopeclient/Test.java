package ru.sstu.vak.periscopeclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public class Test extends AppCompatActivity {

    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        rv = (RecyclerView) findViewById(R.id.rv);
    }

//    //Здесь мы добавляем элемент в набор данных
//    public void addItem(){
//        rv.re
//        //Мы можем вызвать
//        //super.notifyItemInserted(position);
//    }
//
//    //А здесь - удаляем
//    public void deleteItem(int position){
//        this.persons.remove(position);
//        //То же самое с методом
//        //super.notifyItemRemoved(position);
//    }
}
