String.prototype.hashString = function() {
    let hash = 0,
        i, chr;
    if (this.length === 0) return hash;
    for (i = 0; i < this.length; i++) {
        chr = this.charCodeAt(i);
        hash = ((hash << 5) - hash) + chr;
        hash |= 0;
    }

    let result = '';
    for (i = 0; i < 6; i++) {
        const char = (hash & 0xF).toString(16);
        result += char;
        hash >>= 4;
    }
    return result;
}

window.onload = function () {
    const profilePics = document.getElementsByClassName('profile-placeholder');
    for (let i = 0; i < profilePics.length; i++) {
        const profilePic = profilePics[i];
        const clr =Number('0x'+ profilePic.dataset.str.hashString())%360;
        profilePic.style.backgroundColor = 'hsl('+clr +',70%,50%)';
    }
}

