package rx_activity_result;

import android.content.Intent;

import java.io.Serializable;

interface OnResult extends Serializable {
    void response(int resultCode, Intent data);
}
