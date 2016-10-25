package net.ucoz.ksen.cannongame;


import android.media.AudioManager;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainActivityFragment extends Fragment {
    private CannonView cannonView;

    public MainActivityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        cannonView = (CannonView) view.findViewById(R.id.canonView);
        return view;
    }

    // Настройка управления громкостью присоздании активности
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Разрешение для управления громкостью кнопок
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    // При переводе MainActivity в фоновый режим завершается игра
    @Override
    public void onPause() {
        super.onPause();
        cannonView.stopGame(); // Завершение игры
    }

    // При уничтожении MainActivity освобождаются ресурсы
    @Override
    public void onDestroy() {
        super.onDestroy();
        cannonView.releaseResouces();
    }
}
